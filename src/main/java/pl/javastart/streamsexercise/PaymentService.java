package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate))
                .collect(Collectors.toList());
    }


    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */
    List<Payment> findPaymentsSortedByItemCountAsc() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparingInt(i -> i.getPaymentItems().size()))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        return paymentRepository.findAll().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getPaymentItems().size(), s1.getPaymentItems().size()))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate().getMonth().equals(yearMonth.getMonth()))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate().getMonth() == ZonedDateTime.now().getMonth())
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        int size = findPaymentsSortedByDateDesc().size();
        return findPaymentsSortedByDateAsc().stream()
                .skip(Math.max(0, size - days))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll().stream()
                .filter(payment -> (payment.getPaymentItems().size() == 1))
                .collect(Collectors.toSet());

    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getMonth().equals(ZonedDateTime.now().getMonth()))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());

    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getMonth() == yearMonth.getMonth())
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate().getMonth() == yearMonth.getMonth())
                .flatMap(payment -> payment.getPaymentItems().stream())
                .map(PaymentItem::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .collect(Collectors.toList());

    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> {
                    Optional<BigDecimal> suma = payment.getPaymentItems().stream()
                            .map(PaymentItem::getRegularPrice)
                            .reduce((bigDecimal, bigDecimal2) -> bigDecimal.add(bigDecimal2));

                    if (suma.isPresent()) {
                        return suma.get().intValue() > value;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

}
