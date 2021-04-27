FlowField flowfield;
ArrayList<Particle> particles;

boolean debug = false;

void setup() {
  size(600, 600, P2D);
  
  flowfield = new FlowField(90);
  flowfield.update();

  particles = new ArrayList<Particle>();
  for (int i = 0; i < 3000; i++) {
    PVector start = new PVector(random(width), random(height));
    particles.add(new Particle(start, random(2, 8)));
  }
  background(255);
}

void draw() {
  
  flowfield.update();
  
  if (debug) 
    flowfield.display();
  
  for (Particle p : particles) {
    p.follow(flowfield);
    p.run();
  }
}
