import { DocumentStatus } from '../../../shared/models/document-status.model';
import { DocumentClassification } from '../../../shared/models/document-classification.model';

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