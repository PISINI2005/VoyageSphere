import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog } from '../models/compliance.model';

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  private apiUrl = 'http://localhost:8080/api/v1/auditLogs';

  constructor(private http: HttpClient) {}

  getLogs(filters?: any): Observable<AuditLog[]> {
    let params = new HttpParams();
    if (filters?.entityType) params = params.set('entityType', filters.entityType);
    if (filters?.entityId) params = params.set('entityId', filters.entityId);
    if (filters?.userId) params = params.set('userId', filters.userId);
    if (filters?.action) params = params.set('action', filters.action);
    if (filters?.logType) params = params.set('logType', filters.logType);

    return this.http.get<AuditLog[]>(this.apiUrl, { params });
  }
}
