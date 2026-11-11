package org.skillbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.wallet")
public class WalletProperties {

    private BigDecimal pointsToNairaRate = new BigDecimal("10.00");
    private String currency = "PTS";

    public BigDecimal getPointsToNairaRate() { return pointsToNairaRate; }
    public void setPointsToNairaRate(BigDecimal rate) { this.pointsToNairaRate = rate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}