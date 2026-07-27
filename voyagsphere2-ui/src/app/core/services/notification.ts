import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { NotificationResponseDTO } from '../models/travel.model';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/notifications';

  private notificationsSubject = new BehaviorSubject<NotificationResponseDTO[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  constructor(private http: HttpClient) {}

  getMyNotifications(): Observable<NotificationResponseDTO[]> {
    return this.http.get<NotificationResponseDTO[]>(`${this.baseUrl}/me`).pipe(
      tap(notifications => this.notificationsSubject.next(notifications))
    );
  }

  markAsRead(id: number): Observable<NotificationResponseDTO> {
    return this.http.patch<NotificationResponseDTO>(`${this.baseUrl}/${id}/read`, {}).pipe(
      tap(() => this.refreshNotifications())
    );
  }

  markAllAsRead(): Observable<number> {
    return this.http.patch<number>(`${this.baseUrl}/read-all`, {}).pipe(
      tap(() => this.refreshNotifications())
    );
  }

  private refreshNotifications(): void {
    this.http.get<NotificationResponseDTO[]>(`${this.baseUrl}/me`).subscribe(
      notifications => this.notificationsSubject.next(notifications)
    );
  }

  get unreadCount(): number {
    return this.notificationsSubject.value.filter(n => n.status === 'UNREAD').length;
  }
}
