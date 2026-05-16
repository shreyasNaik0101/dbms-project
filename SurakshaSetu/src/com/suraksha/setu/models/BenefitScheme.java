package com.suraksha.setu.models;

/**
 * Represents a social security / insurance benefit scheme.
 */
public class BenefitScheme {
    private int    schemeId;
    private String schemeName;
    private double coverageAmount;
    private double premiumCost;

    public BenefitScheme() {}

    public BenefitScheme(int schemeId, String schemeName,
                         double coverageAmount, double premiumCost) {
        this.schemeId       = schemeId;
        this.schemeName     = schemeName;
        this.coverageAmount = coverageAmount;
        this.premiumCost    = premiumCost;
    }

    public int    getSchemeId()                       { return schemeId; }
    public void   setSchemeId(int schemeId)           { this.schemeId = schemeId; }
    public String getSchemeName()                     { return schemeName; }
    public void   setSchemeName(String schemeName)    { this.schemeName = schemeName; }
    public double getCoverageAmount()                 { return coverageAmount; }
    public void   setCoverageAmount(double amount)    { this.coverageAmount = amount; }
    public double getPremiumCost()                    { return premiumCost; }
    public void   setPremiumCost(double premiumCost)  { this.premiumCost = premiumCost; }

    @Override
    public String toString() {
        return String.format("%s | Coverage: ₹%.0f | Premium: ₹%.0f/mo",
                schemeName, coverageAmount, premiumCost);
    }
}
