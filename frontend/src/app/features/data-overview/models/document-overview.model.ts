import { DocumentStatus } from '../../../shared/models/document-status.model';
import { DocumentClassification } from '../../../shared/models/document-classification.model';

export interface DocumentOverviewDTO {
  id: string;
  title: string;
  description: string;
  status: DocumentStatus;
  classification: DocumentClassification;
  createdAt: Date;
  restaurantName: string;
  userId: string;
  details: string;
}