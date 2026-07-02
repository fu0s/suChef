export interface DocumentDetailsDTO {
  id: string;
  title: string;
  description: string;
  status: DocumentStatus;
  classification: DocumentClassification;
  details: string;
  createdAt: Date;
  userId: string;
  restaurantName: string;
}