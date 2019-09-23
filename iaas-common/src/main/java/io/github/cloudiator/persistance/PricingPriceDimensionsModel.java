package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class PricingPriceDimensionsModel extends Model {
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal pricePerUnit;
    @Column(nullable = false)
    private String unit;
    @Column(nullable = true)
    private String description;
    @Column(nullable = true)
    private String beginRange;
    @Column(nullable = true)
    private String endRange;

    @ManyToOne
    @JoinColumn(name="pricingTermsModelId", nullable=true)
    private PricingTermsModel pricingTermsModel;

    public PricingPriceDimensionsModel(BigDecimal pricePerUnit, String unit, String description, String beginRange, String endRange) {
        this.pricePerUnit = pricePerUnit;
        this.unit = unit;
        this.description = description;
        this.beginRange = beginRange;
        this.endRange = endRange;
    }

    protected PricingPriceDimensionsModel() {
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBeginRange() {
        return beginRange;
    }

    public void setBeginRange(String beginRange) {
        this.beginRange = beginRange;
    }

    public String getEndRange() {
        return endRange;
    }

    public void setEndRange(String endRange) {
        this.endRange = endRange;
    }

    public PricingTermsModel getPricingTermsModel() {
        return pricingTermsModel;
    }

    public void setPricingTermsModel(PricingTermsModel pricingTermsModel) {
        this.pricingTermsModel = pricingTermsModel;
    }
}
