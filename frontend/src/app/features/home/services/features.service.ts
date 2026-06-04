import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Feature, FEATURES } from '../models/feature.model';

/**
 * FeaturesService
 * Provides feature data for the landing page features grid section
 */
@Injectable({
  providedIn: 'root'
})
export class FeaturesService {
  private featuresSubject = new BehaviorSubject<Feature[]>(FEATURES);

  /**
   * Get all features as an observable
   */
  getFeatures(): Observable<Feature[]> {
    return this.featuresSubject.asObservable();
  }

  /**
   * Get features sorted by displayOrder
   */
  getFeaturesOrdered(): Observable<Feature[]> {
    return new Observable(observer => {
      const ordered = [...FEATURES].sort((a, b) => a.displayOrder - b.displayOrder);
      observer.next(ordered);
      observer.complete();
    });
  }

  /**
   * Get a single feature by id
   */
  getFeatureById(id: string): Observable<Feature | undefined> {
    return new Observable(observer => {
      const feature = FEATURES.find(f => f.id === id);
      observer.next(feature);
      observer.complete();
    });
  }

  /**
   * Get features count
   */
  getFeaturesCount(): number {
    return FEATURES.length;
  }
}
