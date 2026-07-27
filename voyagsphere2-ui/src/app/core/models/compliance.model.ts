import { LogType } from '../enums/compliance-enums';
import { AuditEntity } from '../enums/compliance-enums';
import { ComplaintStatus } from '../enums/compliance-enums';

export interface AuditLog {
  auditId: number;
  action: string;
  entityType: AuditEntity;
  entityId: number;
  logType: LogType;
  timestamp: string;
  userId: number;
  userEmail: string;
}

export interface ComplaintStatusUpdateDTO {
  status: ComplaintStatus;
  resolutionNote?: string;
}

export interface Complaint {
  complaintId: number;
  subject: string;
  userId: number;
  status: ComplaintStatus;
  description: string;
  targetId: number;
  createdDate: string;
  resolutionNote?: string;
}