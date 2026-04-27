import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-main',
  imports: [],
  templateUrl: './main.html',
  styleUrl: './main.css'
})
export class Main {
  displayName: string;

  constructor(private auth: AuthService, private router: Router) {
    this.displayName = auth.getDisplayName();
  }

  onLogout() {
    this.auth.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        this.auth.clearTokens();
        this.router.navigate(['/login']);
      }
    });
  }
}
