package com.games.rocketmq.consumer;

import com.games.config.SnowflakeIdGenerator;
import com.games.dto.SportTransactionMessage;
import com.games.entity.Merchant;
import com.games.entity.SportBet;
import com.games.entity.SportTransaction;
import com.games.entity.User;
import com.games.enums.SportTransactionType;
import com.games.repository.MerchantRepository;
import com.games.repository.SportBetRepository;
import com.games.repository.SportTransactionRepository;
import com.games.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportTransactionMessageConsumerTest {

    @Mock private MerchantRepository merchantRepository;
    @Mock private SportTransactionRepository sportTransactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private SportBetRepository sportBetRepository;
    @Mock private SnowflakeIdGenerator idGenerator;

    private SportTransactionMessageConsumer consumer;

    private static final Long USER_ID     = 2001L;
    private static final Long MERCHANT_ID = 1L;
    private static final Long TX_ID       = 88888L;

    @BeforeEach
    void setUp() {
        consumer = new SportTransactionMessageConsumer(
                merchantRepository, sportTransactionRepository,
                userRepository, sportBetRepository, idGenerator);
    }

    // ── happy path ──────────────────────────────────────────────────────────

    @Test
    void onMessage_withoutSportBet_shouldSaveSportTransaction() {
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_REGISTER, null);
        User user         = new User();
        Merchant merchant = new Merchant();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<SportTransaction> captor = ArgumentCaptor.forClass(SportTransaction.class);
        verify(sportTransactionRepository).save(captor.capture());

        SportTransaction saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(TX_ID);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getMerchant()).isEqualTo(merchant);
        assertThat(saved.getType()).isEqualTo(SportTransactionType.SPORT_REGISTER);
        assertThat(saved.getAmount()).isEqualByComparingTo("1000.00");
        assertThat(saved.getBalanceBefore()).isEqualByComparingTo("0.00");
        assertThat(saved.getBalanceAfter()).isEqualByComparingTo("1000.00");
        assertThat(saved.getSportBet()).isNull();
        verify(sportBetRepository, never()).findById(anyLong());
    }

    @Test
    void onMessage_withSportBetId_shouldLookupAndAttachSportBet() {
        Long sportBetId   = 555L;
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_BET, sportBetId);
        SportBet sportBet = mock(SportBet.class);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(new Merchant()));
        when(sportBetRepository.findById(sportBetId)).thenReturn(Optional.of(sportBet));
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<SportTransaction> captor = ArgumentCaptor.forClass(SportTransaction.class);
        verify(sportTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getSportBet()).isEqualTo(sportBet);
    }

    @Test
    void onMessage_withSportBetIdThatDoesNotExist_shouldSaveWithNullBet() {
        Long sportBetId   = 666L;
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_BET, sportBetId);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(new Merchant()));
        when(sportBetRepository.findById(sportBetId)).thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<SportTransaction> captor = ArgumentCaptor.forClass(SportTransaction.class);
        verify(sportTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().getSportBet()).isNull();
    }

    @Test
    void onMessage_sportWin_shouldSaveSportTransaction() {
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_WIN, null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(new Merchant()));
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        verify(sportTransactionRepository).save(any(SportTransaction.class));
    }

    // ── error paths ─────────────────────────────────────────────────────────

    @Test
    void onMessage_whenUserNotFound_shouldThrowAndNotSave() {
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_DEPOSIT, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.onMessage(msg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process sport transaction message");

        verify(sportTransactionRepository, never()).save(any());
    }

    @Test
    void onMessage_whenMerchantNotFound_shouldThrowAndNotSave() {
        SportTransactionMessage msg = buildMessage(SportTransactionType.SPORT_DEPOSIT, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.onMessage(msg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process sport transaction message");

        verify(sportTransactionRepository, never()).save(any());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private SportTransactionMessage buildMessage(SportTransactionType type, Long sportBetId) {
        return new SportTransactionMessage(
                USER_ID, MERCHANT_ID, type,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                "Test sport transaction", sportBetId);
    }
}
