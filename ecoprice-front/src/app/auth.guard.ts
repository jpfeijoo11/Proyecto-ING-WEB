import { inject } from '@angular/core';
import { Router } from '@angular/router';

export const authGuard = () => {
  const router = inject(Router);
  const user = localStorage.getItem('user');

  if (user) {
    
    return true;
  } else {
    
    router.navigate(['/']);
    return false;
  }
};