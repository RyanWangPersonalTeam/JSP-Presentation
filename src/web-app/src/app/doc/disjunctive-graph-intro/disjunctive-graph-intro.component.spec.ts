import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisjunctiveGraphIntroComponent } from './disjunctive-graph-intro.component';

describe('DisjunctiveGraphIntroComponent', () => {
  let component: DisjunctiveGraphIntroComponent;
  let fixture: ComponentFixture<DisjunctiveGraphIntroComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisjunctiveGraphIntroComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisjunctiveGraphIntroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
