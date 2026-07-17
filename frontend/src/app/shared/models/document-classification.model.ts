export enum DocumentClassification {
  BILL = 'BILL',
  ORDER = 'ORDER',
  MENU = 'MENU'
}

export const DocumentClassificationLabels: Record<DocumentClassification, string> = {
  [DocumentClassification.BILL]: 'Bill / Invoice',
  [DocumentClassification.ORDER]: 'Order',
  [DocumentClassification.MENU]: 'Menu'
};

export const DocumentClassificationColors: Record<DocumentClassification, string> = {
  [DocumentClassification.BILL]: 'bg-indigo-100 text-indigo-800',
  [DocumentClassification.ORDER]: 'bg-teal-100 text-teal-800',
  [DocumentClassification.MENU]: 'bg-emerald-100 text-emerald-800'
};

export function getDocumentClassificationLabel(classification: DocumentClassification): string {
  return DocumentClassificationLabels[classification] || classification;
}

export function getDocumentClassificationColor(classification: DocumentClassification): string {
  return DocumentClassificationColors[classification] || 'bg-gray-100 text-gray-800';
}

export interface DocumentClassificationOptions {
  value: DocumentClassification;
  label: string;
}

export const ALL_DOCUMENT_CLASSIFICATIONS: DocumentClassificationOptions[] = [
  { value: DocumentClassification.BILL, label: DocumentClassificationLabels[DocumentClassification.BILL] },
  { value: DocumentClassification.ORDER, label: DocumentClassificationLabels[DocumentClassification.ORDER] },
  { value: DocumentClassification.MENU, label: DocumentClassificationLabels[DocumentClassification.MENU] }
];