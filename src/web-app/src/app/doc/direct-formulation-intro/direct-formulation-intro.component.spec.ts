import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DirectFormulationIntroComponent } from './direct-formulation-intro.component';

describe('DirectFormulationIntroComponent', () => {
  let component: DirectFormulationIntroComponent;
  let fixture: ComponentFixture<DirectFormulationIntroComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DirectFormulationIntroComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DirectFormulationIntroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
