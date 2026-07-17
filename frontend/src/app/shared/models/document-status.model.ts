export enum DocumentStatus {
  RECEIVED = 'RECEIVED',
  PROCESSING = 'PROCESSING',
  OCR_PROCESSING = 'OCR_PROCESSING',
  PENDING_VALIDATION = 'PENDING_VALIDATION',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export const DocumentStatusLabels: Record<DocumentStatus, string> = {
  [DocumentStatus.RECEIVED]: 'Received',
  [DocumentStatus.PROCESSING]: 'Processing',
  [DocumentStatus.OCR_PROCESSING]: 'OCR Processing',
  [DocumentStatus.PENDING_VALIDATION]: 'Pending Validation',
  [DocumentStatus.COMPLETED]: 'Completed',
  [DocumentStatus.FAILED]: 'Failed'
};

export const DocumentStatusColors: Record<DocumentStatus, string> = {
  [DocumentStatus.RECEIVED]: 'bg-blue-100 text-blue-800',
  [DocumentStatus.PROCESSING]: 'bg-yellow-100 text-yellow-800',
  [DocumentStatus.OCR_PROCESSING]: 'bg-orange-100 text-orange-800',
  [DocumentStatus.PENDING_VALIDATION]: 'bg-purple-100 text-purple-800',
  [DocumentStatus.COMPLETED]: 'bg-green-100 text-green-800',
  [DocumentStatus.FAILED]: 'bg-red-100 text-red-800'
};

export function getDocumentStatusLabel(status: DocumentStatus): string {
  return DocumentStatusLabels[status] || status;
}

export function getDocumentStatusColor(status: DocumentStatus): string {
  return DocumentStatusColors[status] || 'bg-gray-100 text-gray-800';
}