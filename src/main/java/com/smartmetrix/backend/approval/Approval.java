
package com.smartmetrix.backend.approval;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "approvals")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Inspection ID is required")
    private Long inspectionId;

    private Long userId;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Remarks are required")
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    @Column(length = 1000)
    private String remarks;

    public Approval() {
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
