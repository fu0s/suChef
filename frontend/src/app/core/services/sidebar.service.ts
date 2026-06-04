import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class SidebarService {
    private isCollapsedSubject = new BehaviorSubject<boolean>(true); // Default to collapsed
    public isCollapsed$: Observable<boolean> = this.isCollapsedSubject.asObservable();

    constructor() {
        // Load state from localStorage if needed, or default to true
        const saved = localStorage.getItem('sidebar_collapsed');
        if (saved !== null) {
            this.isCollapsedSubject.next(saved === 'true');
        }
    }

    toggle(): void {
        const newState = !this.isCollapsedSubject.value;
        this.setCollapsed(newState);
    }

    setCollapsed(collapsed: boolean): void {
        this.isCollapsedSubject.next(collapsed);
        localStorage.setItem('sidebar_collapsed', collapsed.toString());
    }

    get isCollapsed(): boolean {
        return this.isCollapsedSubject.value;
    }
}
