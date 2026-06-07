import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentsService, RestaurantMetrics } from '../../../core/services/documents.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { AuthService, User } from '../../../core/services/auth.service';
import { AuthContext } from '../../../core/services/auth-context.service';
import { NgApexchartsModule } from 'ng-apexcharts';
import { Observable } from 'rxjs';

import {
  ApexAxisChartSeries,
  ApexNonAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexTitleSubtitle,
  ApexTheme,
  ApexResponsive,
  ApexDataLabels,
  ApexLegend,
  ApexPlotOptions,
  ApexFill,
  ApexTooltip,
  ApexStroke
} from 'ng-apexcharts';

export type ChartOptions = {
  series: ApexAxisChartSeries | ApexNonAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  title: ApexTitleSubtitle;
  labels: string[];
  theme: ApexTheme;
  responsive: ApexResponsive[];
  dataLabels: ApexDataLabels;
  legend: ApexLegend;
  plotOptions: ApexPlotOptions;
  colors: string[];
  fill: ApexFill;
  tooltip: ApexTooltip;
  stroke: ApexStroke;
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, NgApexchartsModule],
  templateUrl: './dashboard.component.html',
  styles: [`
    :host {
      display: block;
      padding: 2rem 0;
    }
  `]
})
export class DashboardComponent implements OnInit {
  metrics: RestaurantMetrics = {
    totalRevenue: 0,
    totalExpenses: 0,
    totalOrders: 0,
    averageOrderValue: 0,
    profitMargin: 0,
    documentCount: 0,
    bestOrder: 0,
    highestNetMonth: 'N/A',
    stockWasteImprovement: '0%',
    costOptimization: 0,
    wasteReduction: 0,
    qualityTracking: 'N/A',
    revenueBreakdown: [],
    costAnalysis: [],
    profitMetrics: [],
    performanceTracking: [],
    topDishAnalytics: [],
    marginOptimization: 0
  };

  currentUser: User | null = null;
  newRestaurantName: string = '';
  isSettingRestaurant: boolean = false;

  metricsLoading$: Observable<boolean>;
  metricsError$: Observable<string | null>;

  // Chart Properties
  public revenuePieChartOptions: Partial<ChartOptions> = {};
  public performanceLineChartOptions: Partial<ChartOptions> = {};
  public topDishesBarChartOptions: Partial<ChartOptions> = {};

  constructor(
    private documentsService: DocumentsService,
    private authService: AuthService,
    private authContext: AuthContext
  ) {
    this.metricsLoading$ = this.documentsService.metricsLoading$;
    this.metricsError$ = this.documentsService.metricsError$;
  }

  ngOnInit(): void {
    this.authContext.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user && user.restaurantName) {
        this.documentsService.metrics$.subscribe(metrics => {
          this.metrics = metrics;
          this.initCharts();
        });
      }
    });
  }

  submitRestaurantName(): void {
    if (this.newRestaurantName.trim()) {
      this.isSettingRestaurant = true;
      this.authService.setRestaurant(this.newRestaurantName).subscribe({
        next: () => {
          this.isSettingRestaurant = false;
        },
        error: (err) => {
          console.error('Failed to set restaurant', err);
          this.isSettingRestaurant = false;
        }
      });
    }
  }

  dismissMetricsError(): void {
    this.documentsService.clearMetricsError();
  }

  private initCharts() {
    this.initRevenuePieChart();
    this.initPerformanceChart();
    this.initTopDishesChart();
  }

  private initRevenuePieChart() {
    if (!this.metrics.revenueBreakdown || this.metrics.revenueBreakdown.length === 0) return;

    // Extract labels and series from the array
    const labels = this.metrics.revenueBreakdown.map(item => item.category);
    const series = this.metrics.revenueBreakdown.map(item => Number(item.amount));

    this.revenuePieChartOptions = {
      series: series,
      chart: {
        width: '100%',
        type: 'donut',
        height: 320,
        animations: { enabled: true, speed: 800 }
      },
      labels: labels,
      dataLabels: { enabled: false },
      legend: { position: 'bottom', fontSize: '14px', fontFamily: 'inherit' },
      theme: { mode: 'light' },
      colors: ['#A0632A', '#E8A045', '#6B8E71', '#D4AF37', '#C17F4B'],
      stroke: { show: true, width: 2, colors: ['#fff'] }
    };
  }

  private initPerformanceChart() {
    if (!this.metrics.performanceTracking || this.metrics.performanceTracking.length === 0) return;

    const data = this.metrics.performanceTracking.map(item => Number(item.revenue));
    const categories = this.metrics.performanceTracking.map(item => item.month);

    this.performanceLineChartOptions = {
      series: [{ name: "Revenue", data: data }],
      chart: {
        height: 350,
        type: 'area',
        toolbar: { show: false },
        animations: { enabled: true, speed: 800 }
      },
      colors: ['#A0632A'],
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: 3 },
      fill: {
        type: 'gradient',
        gradient: {
          shadeIntensity: 1,
          opacityFrom: 0.4,
          opacityTo: 0.0,
          stops: [0, 90, 100]
        }
      },
      xaxis: {
        categories: categories,
        labels: { style: { fontFamily: 'inherit' } },
        axisBorder: { show: false },
        axisTicks: { show: false }
      }
    };
  }

  private initTopDishesChart() {
    if (!this.metrics.topDishAnalytics || this.metrics.topDishAnalytics.length === 0) return;

    const data = this.metrics.topDishAnalytics.map(item => Number(item.value));
    const categories = this.metrics.topDishAnalytics.map(item => item.name);

    this.topDishesBarChartOptions = {
      series: [{ name: "Orders", data: data }],
      chart: {
        type: 'bar',
        toolbar: { show: false },
        animations: { enabled: true, speed: 800 }
      },
      plotOptions: {
        bar: {
          horizontal: true,
          borderRadius: 4,
          dataLabels: { position: 'top' }
        }
      },
      colors: ['#E8A045'],
      dataLabels: {
        enabled: true,
        style: { fontFamily: 'inherit', colors: ['#fff'] }
      },
      stroke: { show: true, width: 1, colors: ['#fff'] },
      xaxis: {
        categories: categories,
        labels: { style: { fontFamily: 'inherit' } },
        axisBorder: { show: false },
        axisTicks: { show: false }
      }
    };
  }
}