package com.suraksha.setu.models;

import java.sql.Timestamp;

/** Represents a KYC document uploaded by a worker. */
public class KycDocument {
    private int       docId;
    private int       workerId;
    private String    documentType;
    private String    status;        // Verified / Pending / Rejected
    private Timestamp uploadDate;

    public KycDocument() {}

    public KycDocument(int docId, int workerId, String documentType,
                       String status, Timestamp uploadDate) {
        this.docId        = docId;
        this.workerId     = workerId;
        this.documentType = documentType;
        this.status       = status;
        this.uploadDate   = uploadDate;
    }

    public int       getDocId()                       { return docId; }
    public void      setDocId(int docId)              { this.docId = docId; }
    public int       getWorkerId()                    { return workerId; }
    public void      setWorkerId(int workerId)        { this.workerId = workerId; }
    public String    getDocumentType()                { return documentType; }
    public void      setDocumentType(String type)     { this.documentType = type; }
    public String    getStatus()                      { return status; }
    public void      setStatus(String status)         { this.status = status; }
    public Timestamp getUploadDate()                  { return uploadDate; }
    public void      setUploadDate(Timestamp date)    { this.uploadDate = date; }

    @Override
    public String toString() {
        return String.format("KYC{type='%s', status='%s'}", documentType, status);
    }
}
