package com.games.rocketmq.consumer;

import com.games.config.SnowflakeIdGenerator;
import com.games.dto.TransactionMessage;
import com.games.entity.Bet;
import com.games.entity.Merchant;
import com.games.entity.Transaction;
import com.games.entity.User;
import com.games.enums.TransactionType;
import com.games.repository.BetRepository;
import com.games.repository.MerchantRepository;
import com.games.repository.TransactionRepository;
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
class TransactionMessageConsumerTest {

    @Mock private MerchantRepository merchantRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private BetRepository betRepository;
    @Mock private SnowflakeIdGenerator idGenerator;

    private TransactionMessageConsumer consumer;

    private static final Long USER_ID     = 1001L;
    private static final Long MERCHANT_ID = 1L;
    private static final Long TX_ID       = 99999L;

    @BeforeEach
    void setUp() {
        consumer = new TransactionMessageConsumer(
                merchantRepository, transactionRepository,
                userRepository, betRepository, idGenerator);
    }

    // ── happy path ──────────────────────────────────────────────────────────

    @Test
    void onMessage_withoutBet_shouldSaveTransaction() {
        TransactionMessage msg = buildMessage(TransactionType.REGISTER, null);
        User user         = new User();
        Merchant merchant = new Merchant();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant));
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(TX_ID);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getMerchant()).isEqualTo(merchant);
        assertThat(saved.getType()).isEqualTo(TransactionType.REGISTER);
        assertThat(saved.getAmount()).isEqualByComparingTo("50.00");
        assertThat(saved.getBalanceBefore()).isEqualByComparingTo("0.00");
        assertThat(saved.getBalanceAfter()).isEqualByComparingTo("50.00");
        assertThat(saved.getBet()).isNull();
        verify(betRepository, never()).findById(anyLong());
    }

    @Test
    void onMessage_withBetId_shouldLookupAndAttachBet() {
        Long betId        = 777L;
        TransactionMessage msg = buildMessage(TransactionType.BET, betId);
        Bet bet           = new Bet();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(new Merchant()));
        when(betRepository.findById(betId)).thenReturn(Optional.of(bet));
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getBet()).isEqualTo(bet);
    }

    @Test
    void onMessage_withBetIdThatDoesNotExist_shouldSaveTransactionWithNullBet() {
        Long betId        = 888L;
        TransactionMessage msg = buildMessage(TransactionType.BET, betId);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(new Merchant()));
        when(betRepository.findById(betId)).thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(TX_ID);

        consumer.onMessage(msg);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getBet()).isNull();
    }

    // ── error paths ─────────────────────────────────────────────────────────

    @Test
    void onMessage_whenUserNotFound_shouldThrowAndNotSave() {
        TransactionMessage msg = buildMessage(TransactionType.DEPOSIT, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.onMessage(msg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process transaction message");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void onMessage_whenMerchantNotFound_shouldThrowAndNotSave() {
        TransactionMessage msg = buildMessage(TransactionType.DEPOSIT, null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User()));
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.onMessage(msg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process transaction message");

        verify(transactionRepository, never()).save(any());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private TransactionMessage buildMessage(TransactionType type, Long betId) {
        return new TransactionMessage(
                USER_ID, MERCHANT_ID, type,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                "Test transaction", betId);
    }
}
