export interface DocumentOverviewDTO {
  id: string;
  title: string;
  description: string;
  status: DocumentStatus;
  classification: DocumentClassification;
  createdAt: Date;
}