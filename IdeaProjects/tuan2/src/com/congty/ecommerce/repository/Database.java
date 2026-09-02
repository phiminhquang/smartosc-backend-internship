package com.congty.ecommerce.repository;

import com.congty.ecommerce.model.Voucher; // Bắt buộc phải import vì khác package
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Database {
    private List<Voucher> vouchers = Arrays.asList(
            new Voucher("TET2026", 50000, true),
            new Voucher("HE2025", 20000, false)
    );

    public Optional<Voucher> findVoucher(String inputCode) {
        for (Voucher v : vouchers) {
            if (v.getCode().equals(inputCode)) {
                return Optional.of(v);
            }
        }
        return Optional.empty();
    }
}