import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private themeSubject = new BehaviorSubject<'light' | 'dark'>(this.getInitialTheme());
  public theme$: Observable<'light' | 'dark'> = this.themeSubject.asObservable();

  constructor() {
    this.applyTheme(this.themeSubject.value);
  }

  private getInitialTheme(): 'light' | 'dark' {
    const saved = localStorage.getItem('app-theme');
    if (saved === 'dark' || saved === 'light') {
      return saved;
    }
    return 'light'; // Default to light theme
  }

  toggleTheme(): void {
    const newTheme = this.themeSubject.value === 'light' ? 'dark' : 'light';
    this.setTheme(newTheme);
  }

  setTheme(theme: 'light' | 'dark'): void {
    localStorage.setItem('app-theme', theme);
    this.themeSubject.next(theme);
    this.applyTheme(theme);
  }

  private applyTheme(theme: 'light' | 'dark'): void {
    const htmlElement = document.documentElement;
    const bodyElement = document.body;
    
    if (theme === 'dark') {
      htmlElement.setAttribute('data-bs-theme', 'dark');
      htmlElement.setAttribute('data-theme', 'dark');
      bodyElement.setAttribute('data-theme', 'dark');
      bodyElement.classList.add('dark-theme');
      bodyElement.classList.remove('light-theme');
    } else {
      htmlElement.setAttribute('data-bs-theme', 'light');
      htmlElement.setAttribute('data-theme', 'light');
      bodyElement.setAttribute('data-theme', 'light');
      bodyElement.classList.add('light-theme');
      bodyElement.classList.remove('dark-theme');
    }
  }

  getCurrentTheme(): 'light' | 'dark' {
    return this.themeSubject.value;
  }
}
