package com.games.service;

import com.games.config.SnowflakeIdGenerator;
import com.games.constant.GlobeConstant;
import com.games.constant.RedisConstant;
import com.games.dto.AuthResponse;
import com.games.dto.LoginRequest;
import com.games.dto.RegisterRequest;
import com.games.entity.User;
import com.games.enums.TransactionType;
import com.games.lock.RedisLock;
import com.games.repository.UserRepository;
import com.games.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${register.initial-balance}")
    private BigDecimal registerInitialBalance;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WalletService walletService;
    private final TokenService tokenService;
    private final SnowflakeIdGenerator idGenerator;
    private final RedisLock redisLock;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON
                + RedisConstant.REGISTER + request.getUsername();
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock " + lockKey);
        }

        User user;
        String token;
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("Username already exists: {}", request.getUsername());
                throw new RuntimeException("Username already exists");
            }

            user = createUserWithTransaction(request);
            log.info("User created successfully in database: {}, ID: {}", user.getUsername(), user.getId());

            token = jwtUtil.generateToken(user.getUsername());
            log.debug("JWT token generated for user: {}", user.getUsername());

            try {
                tokenService.storeToken(user.getUsername(), token);
                log.info("Token stored in Redis for user: {}", user.getUsername());
            } catch (Exception e) {
                log.error("Failed to store token in Redis for user: {}", user.getUsername(), e);
                throw new RuntimeException("Failed to store token in Redis: " + e.getMessage(), e);
            }
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }

        log.info("User registration completed successfully: {}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getBalance());
    }

    @Transactional
    protected User createUserWithTransaction(RegisterRequest request) {
        log.debug("Creating user with transaction: {}", request.getUsername());

        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(registerInitialBalance);

        user = userRepository.save(user);
        log.debug("User saved to database: ID={}, username={}", user.getId(), user.getUsername());

        walletService.createTransaction(user, TransactionType.REGISTER, registerInitialBalance,
                BigDecimal.ZERO, registerInitialBalance, "Initial balance on registration", null);
        log.debug("Initial transaction created for user: {}", user.getUsername());

        return user;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        try {
            tokenService.storeToken(user.getUsername(), token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store token in Redis: " + e.getMessage(), e);
        }

        return new AuthResponse(token, user.getUsername(), user.getBalance());
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void logout(String username) {
        tokenService.removeToken(username);
    }
}
