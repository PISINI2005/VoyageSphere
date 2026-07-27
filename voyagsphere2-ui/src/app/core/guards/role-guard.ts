import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const expectedRole = route.data['role'];
  const user = localStorage.getItem('user');

  if (!user) {
    router.navigate(['/']);
    return false;
  }

  const userData = JSON.parse(user);
  if (userData.role === expectedRole) {
    return true;
  }

  router.navigate(['/']);
  return false;
};
