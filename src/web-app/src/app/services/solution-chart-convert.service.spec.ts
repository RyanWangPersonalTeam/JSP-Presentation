import { TestBed } from '@angular/core/testing';

import { SolutionChartConvertService } from './solution-chart-convert.service';

describe('SolutionChartConvertService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SolutionChartConvertService = TestBed.get(SolutionChartConvertService);
    expect(service).toBeTruthy();
  });
});
