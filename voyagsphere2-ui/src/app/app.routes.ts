import { Routes } from '@angular/router';

import { AuthComponent } from './pages/auth/auth';
import { SearchHomeComponent } from './pages/search-home/search-home';
import { SearchResultsComponent } from './pages/search-results/search-results';
import { ServiceDetailsComponent } from './pages/service-details/service-details';
import { BookingComponent } from './pages/booking/booking';
import { PaymentComponent } from './pages/payment/payment';
import { BookingSuccessComponent } from './pages/booking-success/booking-success';
import { PaymentGateway } from './pages/payment-gateway/payment-gateway';
import { MyTripsComponent } from './pages/my-trips/my-trips';
import { ProfilesComponent } from './pages/profiles/profiles';
import { ItinerariesComponent } from './pages/itineraries/itineraries';
import { InvoicesComponent } from './pages/invoices/invoices';
import { SupportComponent } from './pages/support/support';
import { UserProfileComponent } from './pages/user-profile/user-profile.component';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { BookingRequestFormComponent } from './pages/booking-request/booking-request-form/booking-request-form.component';
import { BookingRequestDashboardComponent } from './pages/booking-request/booking-request-dashboard/booking-request-dashboard.component';
import { AgentBookingRequestComponent } from './pages/agent/booking-request/agent-booking-request.component';
import { AgentBookingRequestDetailComponent } from './pages/agent/booking-request/agent-booking-request-detail.component';
import { AgentDashboardComponent } from './pages/agent/agent-dashboard/agent-dashboard';
import { CustomersComponent } from './pages/agent/customers/customers';
import { CustomerDetailsComponent } from './pages/agent/customer-details/customer-details';
import { CustomerBookings } from './pages/agent/customer-bookings/customer-bookings';
import { Bookings } from './pages/agent/bookings/bookings';
import { Itineraries } from './pages/agent/itineraries/itineraries';
import { Passengers } from './pages/agent/passengers/passengers';
import { BookingDetails } from './pages/agent/booking-details/booking-details';
import { ItineraryDetails } from './pages/agent/itinerary-details/itinerary-details';
import { ItineraryCreate } from './pages/agent/itinerary-create/itinerary-create';
import { PassengerEdit } from './pages/agent/passenger-edit/passenger-edit';

import { Dashboard as AdminDashboard } from './pages/admin/dashboard/dashboard';
import { Booking as AdminBooking } from './pages/admin/booking/booking';
import { ComplaintList as AdminComplaint } from './pages/admin/complaint/complaint';
import { Kpireport as AdminKpireport, Kpireport } from './pages/admin/kpireport/kpireport';
import { Payment as AdminPayment, Payment } from './pages/admin/payment/payment';
import { Popup as AdminPopup } from './pages/admin/popupwindow/popupwindow';
import { Sidebar as AdminSidebar } from './pages/admin/sidebar/sidebar';
import { AddTransport } from './pages/admin/transport/add-transport/add-transport';
import { UpdateTransport } from './pages/admin/transport/update-transport/update-transport';
import { ViewTransport } from './pages/admin/transport/view-transport/view-transport';
import { AddPackage } from './pages/admin/travelpackage/add-travelpackage/add-travelpackage';
import { UpdatePackage } from './pages/admin/travelpackage/update-travelpackage/update-travelpackage';
import { ViewPackage } from './pages/admin/travelpackage/view-travelpackage/view-travelpackage';
import { AddUser } from './pages/admin/user/add-user/add-user';
import { ViewUsers } from './pages/admin/user/view-user/view-user';
import { AddFlight } from './pages/admin/flight/add-flight/add-flight';
import { UpdateFlight } from './pages/admin/flight/update-flight/update-flight';
import { ViewFlight } from './pages/admin/flight/view-flight/view-flight';
import { AddHotel } from './pages/admin/hotel/add-hotel/add-hotel';
import { UpdateHotel } from './pages/admin/hotel/update-hotel/update-hotel';
import { ViewHotel } from './pages/admin/hotel/view-hotel/view-hotel';
import { AddPartner } from './pages/admin/partners/add-partner/add-partner';
import { UpdatePartner } from './pages/admin/partners/update-partner/update-partner';
import { ViewPartner } from './pages/admin/partners/view-partner/view-partner';


import { FinanceLayout } from './layout/finance-layout/finance-layout';
import { Home as FinanceHome } from './pages/finance-officer/home/home';
import { Kpi as FinanceKpi } from './pages/finance-officer/kpi/kpi';
import { Payment as FinancePayment } from './pages/finance-officer/payment/payment';
import { ViewInovice as FinanceInvoice } from './pages/finance-officer/view-inovice/view-inovice';
import { Sidebar as FinanceSidebar } from './pages/finance-officer/sidebar/sidebar';
import { DashboardComponent as ComplianceDashboard } from './pages/compliance/dashboard/dashboard.component';
import { AuditLogList } from './pages/compliance/audit-log/audit-log';
import { ComplaintList } from './pages/compliance/complaint-list/complaint-list';
import { ComplaintDetails } from './pages/compliance/complaint-details/complaint-details';
import { ComplaintUpdateStatus } from './pages/compliance/complaint-update-status/complaint-update-status';
import { AdminLayout } from './layout/admin-layout/admin-layout';
import { ComplianceLayoutComponent } from './layout/compliance-layout-component/compliance-layout-component';

export const routes: Routes = [
  {
    path: '',
    component: AuthComponent
  },
  {
    path: 'search',
    component: SearchHomeComponent
  },
  {
    path: 'results/:type',
    component: SearchResultsComponent
  },
  {
    path: 'details/:type/:id',
    component: ServiceDetailsComponent
  },
  {
    path: 'book/:type/:id',
    component: BookingComponent
  },
  {
    path: 'payment/:bookingId',
    component: PaymentComponent
  },
  {
    path: 'payment-gateway',
    component: PaymentGateway,
    canActivate: [authGuard]
  },
  {
    path: 'success/:bookingId',
    component: BookingSuccessComponent
  },
  {
    path: 'trips',
    component: MyTripsComponent
  },
  {
    path: 'user-profile',
    component: UserProfileComponent,
    canActivate: [authGuard]
  },
  {
    path: 'profiles',
    component: ProfilesComponent
  },
  {
    path: 'itineraries',
    component: ItinerariesComponent
  },
  {
    path: 'invoices',
    component: InvoicesComponent
  },
  {
    path: 'support',
    component: SupportComponent
  },
  {
    path: 'booking-request/new',
    component: BookingRequestFormComponent,
    canActivate: [authGuard]
  },
  {
    path: 'booking-request/my-requests',
    component: BookingRequestDashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'agent/booking-request',
    component: AgentBookingRequestComponent,
    canActivate: [authGuard, roleGuard],
    data: { role: 'TRAVEL_AGENT' }
  },
  {
    path: 'agent/booking-request-detail/:id',
    component: AgentBookingRequestDetailComponent,
    canActivate: [authGuard, roleGuard],
    data: { role: 'TRAVEL_AGENT' }
  },

  //agent routes----
  {
  path: 'agent',
  component: AgentDashboardComponent
},
{
  path: 'agent/customers',
  component: CustomersComponent
},
{
  path: 'agent/customer/:id',
  component: CustomerDetailsComponent
},
{
  path: 'agent/customer-bookings',
  component: CustomerBookings
},
{
  path: 'agent/bookings',
  component: Bookings
},
{
  path: 'agent/itineraries',
  component: Itineraries
},
{
  path: 'agent/passengers',
  component: Passengers
},
{
  path: 'agent/booking-details/:id',
  component: BookingDetails
},
{
  path: 'agent/itinerary-details/:id',
  component: ItineraryDetails
},
{
  path: 'agent/itinerary-create',
  component: ItineraryCreate
},
{
  path: 'agent/passenger-edit',
  component: PassengerEdit
},
{
  path: 'agent/passenger-edit/:id',
  component: PassengerEdit
},

  //admin routes----
  // ADMIN ROUTES
{
  path: 'admin',
  component: AdminLayout,
  canActivate: [authGuard, roleGuard],
  data: { role: 'ADMIN' },

  children: [

    { path: '', component: AdminDashboard },

    { path: 'booking', component: AdminBooking },

    { path: 'complaint', component: AdminComplaint },

    { path: 'kpireport', component: AdminKpireport },

    { path: 'payment', component: AdminPayment },

    { path: 'popup', component: AdminPopup },

    // Users
    { path: 'user/add', component: AddUser },
    { path: 'user/view', component: ViewUsers },
    { path: 'user/view/:id', component: ViewUsers },

    // Partners
    { path: 'partners/add', component: AddPartner },
    { path: 'partners/update', component: UpdatePartner },
    { path: 'partners/view', component: ViewPartner },
    { path: 'partners/view/:id', component: ViewPartner },

    // Flights
    { path: 'flight/add', component: AddFlight },
    { path: 'flight/update', component: UpdateFlight },
    { path: 'flight/view', component: ViewFlight },
    { path: 'flight/view/:id', component: ViewFlight },

    // Hotels
    { path: 'hotel/add', component: AddHotel },
    { path: 'hotel/update', component: UpdateHotel },
    { path: 'hotel/view', component: ViewHotel },
    { path: 'hotel/view/:id', component: ViewHotel },

    // Transport
    { path: 'transport/add', component: AddTransport },
    { path: 'transport/update', component: UpdateTransport },
    { path: 'transport/view', component: ViewTransport },
    { path: 'transport/view/:id', component: ViewTransport },

    // Packages
    { path: 'package/add', component: AddPackage },
    { path: 'package/update', component: UpdatePackage },
    { path: 'package/view', component: ViewPackage },
    { path: 'package/view/:id', component: ViewPackage },

    //Finance
    {path:'finance',component:Kpireport},
    {path:'payments',component:Payment},

    //complaints
    {path:'complaints',component:AdminComplaint},




  ]
},

// Compliance Routes
{
  path: 'compliance',
  component: ComplianceLayoutComponent,
  canActivate: [authGuard, roleGuard],
  data: { role: 'COMPLIANCE_OFFICER' },
  children: [
    {
      path: '',
      component: ComplianceDashboard
    },
    {
      path: 'audit-log',
      component: AuditLogList
    },
    {
      path: 'complaints',
      component: ComplaintList
    },
    {
      path: 'complaints/:id',
      component: ComplaintDetails
    },
    {
      path: 'complaints/update',
      component: ComplaintUpdateStatus
    }
  ]
},
  {
    path: 'finance',
    component: FinanceLayout,
    canActivate: [authGuard, roleGuard],
    data: { role: 'FINANCE_OFFICER' },
    children: [
      { path: '', component: FinanceHome },
      { path: 'kpi', component: FinanceKpi },
      { path: 'payment', component: FinancePayment },
      { path: 'invoice', component: FinanceInvoice },
    ]
  },
  {
    path: '**',
    redirectTo: ''
  },
];
