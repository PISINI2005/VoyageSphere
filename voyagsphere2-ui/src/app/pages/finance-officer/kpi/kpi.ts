import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { NgApexchartsModule } from 'ng-apexcharts';
import { KpiService } from '../../../core/services/kpi-service';
import { KpiDto } from '../../../core/models/Kpidto';
// @ts-ignore
import * as html2pdf from 'html2pdf.js';

@Component({
  selector: 'app-kpi',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgApexchartsModule,
  ],
  templateUrl: './kpi.html',
  styleUrls: ['./kpi.css']
})
export class Kpi implements OnInit {

  revenueChart: any;
  bookingChart: any;
  yearlyRevenueChart: any;

  kpidto!: KpiDto;
  yearlyData: KpiDto[] = [];

  // Default values
  currentMonth = new Date().getMonth() + 1;
  currentYear = new Date().getFullYear();
  isYearOnlyView = false;
  fallbackDate = new Date();

  constructor(
    private kpiservice: KpiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  currentDisplayDate(): Date | string {
    return this.kpidto?.generatedAt || this.fallbackDate;
  }

  loadDashboard(): void {
    // Initial load: Fetch current month's data + the yearly trend graph
    this.kpiservice
      .getKpi(this.currentMonth, this.currentYear)
      .subscribe({
        next: (result: KpiDto) => {
          this.kpidto = result;
          this.loadCharts();
          this.loadYearlyChart(this.currentYear);
        },
        error: err => console.error(err)
      });
  }

  generate(form: NgForm): void {
    const month = Number(form.value.month);
    const year = Number(form.value.year);

    this.currentYear = year;

    if (month === 0) {
      // User selected "Year Only" -> Keep/Fetch current month data for cards AND load yearly chart
      this.isYearOnlyView = true;
      const targetMonth = new Date().getFullYear() === year ? (new Date().getMonth() + 1) : 1; // Fallback to Jan if checking a different year

      this.kpiservice.getKpi(targetMonth, year).subscribe({
        next: (result: KpiDto) => {
          this.kpidto = result;
          this.loadCharts();
          this.loadYearlyChart(year);
        },
        error: err => console.error(err)
      });
    } else {
      // User selected a specific Month + Year -> Show month data, hide yearly trend chart
      this.isYearOnlyView = false;
      this.currentMonth = month;

      this.kpiservice.getKpi(month, year).subscribe({
        next: (result: KpiDto) => {
          this.kpidto = result;
          this.loadCharts();
          this.yearlyRevenueChart = null; // Clear the yearly graph line
          this.cdr.detectChanges();
        },
        error: err => console.error(err)
      });
    }
  }

  loadYearlyChart(year: number): void {
    this.kpiservice
      .getKpi(undefined, year)
      .subscribe({
        next: (result: KpiDto[]) => {
          this.yearlyData = result || [];

          const allMonthsLabels = [
            'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
            'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
          ];

          const formattedSeriesData: number[] = [];

          for (let i = 1; i <= 12; i++) {
            const matchedData = this.yearlyData.find(x => {
              const label = (x.reportLabel || '').toLowerCase();
              return label.includes(allMonthsLabels[i - 1].toLowerCase());
            });
            // If data doesn't exist for a month, push 0 to keep the line continuous
            formattedSeriesData.push(matchedData ? (matchedData.totalRevenue || 0) : 0);
          }

          this.yearlyRevenueChart = {
            series: [{ name: 'Revenue', data: formattedSeriesData }],
            chart: {
              type: 'line',
              height: 350,
              toolbar: { show: false },
              fontFamily: 'Inter, sans-serif'
            },
            fill: {
              type: 'gradient',
              gradient: {
                shadeIntensity: 1,
                opacityFrom: 0.4,
                opacityTo: 0.1,
                stops: [0, 90, 100]
              }
            },
            stroke: { curve: 'smooth', width: 3 },
            markers: {
              size: 5,
              colors: ['#6366F1'],
              strokeColors: '#fff',
              strokeWidth: 2
            },
            dataLabels: { enabled: false },
            colors: ['#6366F1'],
            xaxis: {
              categories: allMonthsLabels,
              axisBorder: { show: false },
              axisTicks: { show: false }
            },
            grid: { borderColor: '#f1f5f9' }
          };

          this.cdr.detectChanges();
        },
        error: err => console.error(err)
      });
  }

  downloadPDF(): void {
    const element = document.getElementById('dashboard-pdf-content');
    if (!element) return;

    const options = {
      margin:       0.4,
      filename:     `KPI_Dashboard_Report_${this.currentYear}.pdf`,
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  { scale: 2, useCORS: true, logging: false },
      jsPDF:        { unit: 'in', format: 'letter', orientation: 'landscape' }
    };

    const exporter = (html2pdf as any).default || html2pdf;
    exporter().from(element).set(options).save();
  }

  loadCharts(): void {
    if (!this.kpidto) return;

    this.revenueChart = {
      series: [{
        name: 'Revenue',
        data: [
          this.kpidto.flightRevenue || 0,
          this.kpidto.hotelRevenue || 0,
          this.kpidto.transportRevenue || 0,
          this.kpidto.packageRevenue || 0
        ]
      }],
      chart: {
        type: 'bar',
        height: 320,
        toolbar: { show: false },
        fontFamily: 'Inter, sans-serif',
        animations: { enabled: false }
      },
      plotOptions: {
        bar: {
          columnWidth: '50%',
          distributed: true,
          borderRadius: 6,
          dataLabels: { position: 'top' }
        }
      },
      colors: ['#4F46E5', '#06B6D4', '#10B981', '#F59E0B'],
      dataLabels: {
        enabled: true,
        formatter: (val: number) => "₹" + val.toLocaleString(),
        offsetY: -20,
        style: { fontSize: '11px', colors: ['#475569'] }
      },
      xaxis: {
        categories: ['Flight', 'Hotel', 'Transport', 'Package'],
        axisBorder: { show: false },
        axisTicks: { show: false }
      },
      yaxis: { labels: { formatter: (val: number) => "₹" + val.toLocaleString() } },
      grid: { borderColor: '#f1f5f9' },
      legend: { show: false }
    };

    this.bookingChart = {
      series: [
        this.kpidto.totalBookings || 0,
        this.kpidto.totalCancellations || 0
      ],
      chart: { type: 'pie', height: 320, fontFamily: 'Inter, sans-serif' },
      labels: ['Bookings', 'Cancellations'],
      colors: ['#6366F1', '#EC4899'],
      dataLabels: {
        enabled: true,
        formatter: (val: any, opts: any) => opts.w.config.series[opts.seriesIndex]
      },
      legend: { position: 'bottom', fontFamily: 'Inter, sans-serif', fontWeight: 500 }
    };
  }
}