import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AlgorithmSettingComponent } from './algorithm-setting.component';

describe('AlgorithmSettingComponent', () => {
  let component: AlgorithmSettingComponent;
  let fixture: ComponentFixture<AlgorithmSettingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AlgorithmSettingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlgorithmSettingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
