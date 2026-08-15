import { AfterViewInit, Component, ElementRef, OnDestroy, inject, viewChild } from '@angular/core';
import * as L from 'leaflet';
import { ImovelService } from '../imovel.service';

@Component({
  selector: 'app-mapa',
  imports: [],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss',
})
export class Mapa implements AfterViewInit, OnDestroy {
  private service = inject(ImovelService);
  private readonly container = viewChild.required<ElementRef<HTMLDivElement>>('mapa');
  private map?: L.Map;

  ngAfterViewInit(): void {
    // Centro aproximado do Brasil como estado inicial (ajustado depois pelos pontos).
    this.map = L.map(this.container().nativeElement).setView([-15.78, -47.93], 4);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap',
    }).addTo(this.map);

    this.service.pontos().subscribe((pontos) => {
      const marcadores: L.CircleMarker[] = [];
      for (const p of pontos) {
        const marcador = L.circleMarker([p.latitude, p.longitude], {
          radius: 7,
          color: '#c0392b',
          weight: 2,
          fillColor: '#e74c3c',
          fillOpacity: 0.85,
        }).bindPopup(`<strong>${p.proprietario}</strong><br>${p.municipio}`);
        marcador.addTo(this.map!);
        marcadores.push(marcador);
      }
      // Enquadra o mapa para mostrar todos os pontos.
      if (marcadores.length) {
        this.map!.fitBounds(L.featureGroup(marcadores).getBounds().pad(0.2));
      }
    });
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }
}
