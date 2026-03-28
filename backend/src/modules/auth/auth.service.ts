import { Injectable } from '@nestjs/common';

@Injectable()
export class AuthService {
  isAuthenticated(userId?: string): boolean {
    return Boolean(userId);
  }
}
