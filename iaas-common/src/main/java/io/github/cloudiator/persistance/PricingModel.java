package io.github.cloudiator.persistance;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.TABLE_PER_CLASS)
public class PricingModel extends Model {
    @Column(nullable = false, updatable = false)
    @Lob
    private String cloudUniqueId;
    @Column(nullable = false, updatable = false)
    private String providerId;
    @Column(nullable = false, updatable = false)
    private String name;
    @Column(nullable = false)
    private String instanceName;
    @Column(nullable = false)
    private String cloudServiceProviderName;
    @Column(nullable = false)
    private String currency;
    private String licenseModel;
    private String tenancy;
    @Column(nullable = false)
    private String productFamily;
    private String preInstalledSw;
    private String capacityStatus;
    private String operation;
    @Column(nullable = false)
    private String locationProviderId;
    @ManyToOne(optional = false)
    private OperatingSystemModel operatingSystemModel;
    @ManyToOne(optional = false)
    private ApiModel apiModel;

    @OneToMany(mappedBy="pricingModel", cascade = CascadeType.ALL)
    private List<PricingTermsModel> pricingTermsModels;

    /**
     * Empty constructor for hibernate.
     */
    protected PricingModel() {
    }

    public PricingModel(String cloudUniqueId, String providerId, String name, String instanceName, String cloudServiceProviderName,
                        String licenseModel, String locationProviderId, String currency, String tenancy, OperatingSystemModel operatingSystemModel,
                        List<PricingTermsModel> pricingTermsModels, ApiModel apiModel, String productFamily, String preInstalledSw,
                        String capacityStatus, String operation) { // , DiscoveryItemState state
        this.cloudUniqueId = cloudUniqueId;
        this.providerId = providerId;
        this.name = name;
        this.locationProviderId = locationProviderId;
        this.currency = currency;
        this.tenancy = tenancy;
        this.operatingSystemModel = operatingSystemModel;
        this.pricingTermsModels = pricingTermsModels;
        this.instanceName = instanceName;
        this.cloudServiceProviderName = cloudServiceProviderName;
        this.licenseModel = licenseModel;
        this.apiModel = apiModel;
        this.productFamily = productFamily;
        this.preInstalledSw = preInstalledSw;
        this.capacityStatus = capacityStatus;
        this.operation = operation;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTenancy() {
        return tenancy;
    }

    public void setTenancy(String tenancy) {
        this.tenancy = tenancy;
    }

    public OperatingSystemModel getOperatingSystemModel() {
        return operatingSystemModel;
    }

    public void setOperatingSystemModel(OperatingSystemModel operatingSystemModel) {
        this.operatingSystemModel = operatingSystemModel;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName= instanceName;
    }

    public String getLicenseModel() {
        return licenseModel;
    }

    public void setLicenseModel(String licenseModel) {
        this.licenseModel = licenseModel;
    }

    public List<PricingTermsModel> getPricingTermsModels() {
        return pricingTermsModels;
    }

    public void setPricingTermsModels(List<PricingTermsModel> pricingTermsModels) {
        this.pricingTermsModels = pricingTermsModels;
    }

    public String getCloudUniqueId() {
        return cloudUniqueId;
    }

    public void setCloudUniqueId(String cloudUniqueId) {
        this.cloudUniqueId= cloudUniqueId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId= providerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }

    public String getLocationProviderId() {
        return locationProviderId;
    }

    public void setLocationProviderId(String locationProviderId) {
        this.locationProviderId = locationProviderId;
    }

    public ApiModel getApiModel() {
        return apiModel;
    }

    public void setApiModel(ApiModel apiModel) {
        this.apiModel = apiModel;
    }

    public String getCloudServiceProviderName() {
        return cloudServiceProviderName;
    }

    public void setCloudServiceProviderName(String cloudServiceProviderName) {
        this.cloudServiceProviderName = cloudServiceProviderName;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getPreInstalledSw() {
        return preInstalledSw;
    }

    public void setPreInstalledSw(String preInstalledSw) {
        this.preInstalledSw = preInstalledSw;
    }

    public String getCapacityStatus() {
        return capacityStatus;
    }

    public void setCapacityStatus(String capacityStatus) {
        this.capacityStatus = capacityStatus;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
