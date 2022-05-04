import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-throbber',
  templateUrl: './throbber.component.html',
  styleUrls: ['./throbber.component.scss']
})
export class ThrobberComponent implements OnInit, AfterViewInit {
  @ViewChild("fixture") fixture?: ElementRef<HTMLDivElement>;

  private width = 20;
  private height = 20;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    if (this.fixture) {
      const svg = this.svg(this.fixture.nativeElement);
      this.width = this.fixture.nativeElement.clientWidth;
      this.height = this.fixture.nativeElement.clientHeight;
      this.addThrobber(svg)
    }
  }

  private svg(fixture: HTMLDivElement): d3.Selection<SVGSVGElement, unknown, any, any> {
    return d3.select(fixture)
             .append('svg')
             .attr('width', this.width)
             .attr('height', this.height);
  }

  private addThrobber(svg: d3.Selection<SVGSVGElement, any, any, any>) {
    svg.selectAll('g.throbber')
       .data([1])
       .join(
         enter => {
           const circle = enter
             .append('g')
             .attr('class', 'throbber')
             .append('circle');

           const pulse = (x: d3.Selection<SVGCircleElement, any, any, any>) =>
             x.transition()
              .ease(d3.easeCircle)
              .duration(1000)
              .attr('r', this.width / 8)
              .on('end', () => circle.transition()
                                     .ease(d3.easeCircle)
                                     .duration(1000)
                                     .attr('r', this.width / 2 - 2)
                                     .on('end', x => circle.call(pulse))
              )
           circle
             .attr('r', this.width / 2)
             .attr('cx', this.width / 2)
             .attr('cy', this.width / 2)
             .call(pulse);

           return circle;
         }
         ,
         update => update,
         exit => exit.remove()
       )
  }
}
