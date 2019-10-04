package io.github.cloudiator.persistance;

import javax.persistence.*;
import java.util.List;

@Entity
public class PricingTermsModel extends Model {
    @Column(nullable = false)
    private String termsType; // e.g. OnDemand, Reserved
    private String leaseContractLength;
    private String offeringClass;
    private String purchaseOption;

    @ManyToOne
    @JoinColumn(name="pricingModelId", nullable=false)
    private PricingModel pricingModel;

    @OneToMany(mappedBy="pricingTermsModel", cascade = CascadeType.ALL)
    private List<PricingPriceDimensionsModel> pricingPriceDimensionsModels;

    public PricingTermsModel(String leaseContractLength, String offeringClass, String purchaseOption, List<PricingPriceDimensionsModel> pricingPriceDimensionsModels, String termsType, PricingModel pricingModel) {
        this.leaseContractLength = leaseContractLength;
        this.offeringClass = offeringClass;
        this.purchaseOption = purchaseOption;
        this.pricingPriceDimensionsModels = pricingPriceDimensionsModels;
        this.termsType = termsType;
        this.pricingModel = pricingModel;
    }

    protected PricingTermsModel() {
    }

    public String getLeaseContractLength() {
        return leaseContractLength;
    }

    public void setLeaseContractLength(String leaseContractLength) {
        this.leaseContractLength = leaseContractLength;
    }

    public String getOfferingClass() {
        return offeringClass;
    }

    public void setOfferingClass(String offeringClass) {
        this.offeringClass = offeringClass;
    }

    public String getPurchaseOption() {
        return purchaseOption;
    }

    public void setPurchaseOption(String purchaseOption) {
        this.purchaseOption = purchaseOption;
    }

    public List<PricingPriceDimensionsModel> getPricingPriceDimensionsModels() {
        return pricingPriceDimensionsModels;
    }

    public void setPricingPriceDimensionsModels(List<PricingPriceDimensionsModel> pricingPriceDimensionsModels) {
        this.pricingPriceDimensionsModels = pricingPriceDimensionsModels;
    }

    public String getTermsType() {
        return termsType;
    }

    public void setTermsType(String termsType) {
        this.termsType = termsType;
    }

    public PricingModel getPricingModel() {
        return pricingModel;
    }

    public void setPricingModel(PricingModel pricingModel) {
        this.pricingModel = pricingModel;
    }
}
