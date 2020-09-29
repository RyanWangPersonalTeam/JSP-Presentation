import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from '../app/home/home.component';
import { DirectFormulationIntroComponent} from '../app/doc/direct-formulation-intro/direct-formulation-intro.component';
import { DisjunctiveGraphIntroComponent} from '../app/doc/disjunctive-graph-intro/disjunctive-graph-intro.component';

const routes: Routes = [
  { path:  '',pathMatch: 'full', component:  HomeComponent},
  { path:  'direct-formulation-intro',pathMatch: 'full', component:  DirectFormulationIntroComponent},
  { path:  'disjunctive-graph-intro',pathMatch: 'full', component:  DisjunctiveGraphIntroComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
