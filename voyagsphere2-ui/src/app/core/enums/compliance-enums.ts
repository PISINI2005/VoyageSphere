export enum AuditEntity {
  BOOKING = 'BOOKING',
  COMPLAINT = 'COMPLAINT',
  TRAVELER = 'TRAVELER',
  PAYMENT = 'PAYMENT'
}

export enum ComplaintStatus {
  Open = 'PENDING',
  InProgress = 'IN_PROGRESS',
  Resolved = 'RESOLVED',
  Closed = 'REJECTED'
}

export enum LogType {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR'
}
