/*!
 * Sphere.js JavaScript Library v0.2
 * https://github.com/SamHasler/sphere
 *
 * Copyright 2012 Samuel David Hasler
 * Released under the MIT license
 */

// Description
// -----------

// **Sphere** renders a mathematically perfect textured sphere.
// It calculates the surface of the sphere instead of approximating it with triangles.

/*jshint laxcomma: true, laxbreak: true, browser: true */

function Sphere(canvas, image) {
	"use strict";

  this.opts = { tilt: 40, turn: 20, fpr : 128 };
  this.textureUrl = image;

  // frame count, current angle of rotation. inc/dec to turn.
  this.frame_count = 10000;
  this.gCanvas = canvas;
  this.gCtx = null;
  this.gImage = image;
  this.gCtxImg = null;

  //Variable to hold the size of the canvas
  this.size = 0;

  this.canvasImageData;
  this.textureImageData;

  // Number of frames for one complete rotation.
  this.fpr = 800;
  
  // Constants for indexing dimensions
  this.X=0;
  this.Y=1;
  this.Z=2;

  // vertical and horizontal position on canvas
  this.v = 0;
  this.h = 0;

  this.textureWidth = 0;
  this.textureHeight = 0;

  this.hs=30;            // Horizontal scale of viewing area
  this.vs=30;            // Vertical scale of viewing area

  // NB    The viewing area is an abstract rectangle in the 3d world and is not
  //    the same as the canvas used to display the image.

  this.F = [0,0,0];    // Focal point of viewer
  this.S = [0,30,0];    // Centre of sphere/planet

  this.r=12;            // Radius of sphere/planet

  // Distance of the viewing area from the focal point. This seems
  // to give strange results if it is not equal to S[Y]. It should
  // theoreticaly be changable but hs & vs can still be used along
  // with r to change how large the sphere apears on the canvas.
  // HOWEVER, the values of hs, vs, S[Y], f & r MUST NOT BE TOO BIG
  // as this will result in overflow errors which are not traped
  // and do not stop the script but will result in incorrect
  // displaying of the texture upon the sphere.
  this.f = 30;


  // There may be a solution to the above problem by finding L in
  // a slightly different way.
  // Since the problem is equivelent to finding the intersection
  // in 2D space of a line and a circle then each view area pixel
  // and associated vector can be used define a 2D plane in the 3D
  // space that 'contains' the vector S-F which is the focal point
  // to centre of the sphere.
  //
  // This is essentialy the same problem but I belive/hope it will
  // not result in the same exact solution. I have hunch that the
  // math will not result in such big numbers. Since this abstract
  // plane will be spinning, it may be possible to use the symmetry
  // of the arrangement to reuse 1/4 of the calculations.



  // Variables to hold rotations about the 3 axis
  this.RX = 0;
  this.RY = 0;
  this.RZ = 0;
  // Temp variables to hold them whilst rendering so they won't get updated.
  this.rx = 0;
  this.ry = 0;
  this.rz = 0;

  this.a = 0;
  this.b = 0;
  this.b2 = 0;            // b squared
  this.bx = this.F[this.X] - this.S[this.X];    // = 0 for current values of F and S
  this.by = this.F[this.Y] - this.S[this.Y];
  this.bz = this.F[this.Z] - this.S[this.Z];    // = 0 for current values of F and S

  // c = Fx^2 + Sx^2 -2FxSx + Fy^2 + Sy^2 -2FySy + Fz^2 + Sz^2 -2FzSz - r^2
  // for current F and S this means c = Sy^2 - r^2

  this.c = this.F[this.X] * this.F[this.X] + this.S[this.X] * this.S[this.X]
        + this.F[this.Y] * this.F[this.Y] + this.S[this.Y] * this.S[this.Y]
        + this.F[this.Z] * this.F[this.Z] + this.S[this.Z] * this.S[this.Z]
        - 2*(this.F[this.X]*this.S[this.X] + this.F[this.Y]*this.S[this.Y] + this.F[this.Z]*this.S[this.Z])
        - this.r*this.r
        ;

  this.c4 = this.c*4;        // save a bit of time maybe during rendering

  this.s = 0;

  this.m1 = 0;
  //double m2 = 0;

  // The following are use to calculate the vector of the current pixel to be
  // drawn from the focus position F

  this.hs_ch = 1;                // horizontal scale divided by canvas width
  this.vs_cv = 1;                // vertical scale divided by canvas height
  this.hhs = 0.5 * this.hs;    // half horizontal scale
  this.hvs = 0.5 * this.vs;    // half vertical scale

  this.V = new Array(3);    // vector for storing direction of each pixel from F
  this.L = new Array(3);    // Location vector from S that pixel 'hits' sphere

  this.VY2= this.f * this.f;            // V[Y] ^2  NB May change if F changes
  this.rotCache = {};
};

Sphere.prototype.calcL = function(lx, ly, lz) {
//      var L = new Array(3);
//      L[X]=lx*Math.cos(rz)-ly*Math.sin(rz);
//      L[Y]=lx*Math.sin(rz)+ly*Math.cos(rz);

      var key = ""+ lx +","+ ly +","+ rx;
      if (this.rotCache[key] == null){
        this.rotCache[key] = 1;
      } else {
        this.rotCache[key] = this.rotCache[key]+1;
      }
};
    
Sphere.prototype.calculateVector = function(h, v) {
    // Calculate vector from focus point (Origin, so can ignor) to pixel
    this.V[this.X] = (this.hs_ch * h) - this.hhs;

    // V[Y] always the same as view frame doesn't mov
    this.V[this.Z] = (this.vs_cv * v) - this.hvs;

    // Vector (L) from S where m*V (m is an unknown scalar) intersects
    // surface of sphere is as follows
    //
    // <pre>
    // L = F + mV - S
    //
    //    ,-------.
    //   /         \ -----m------
    //  |     S<-L->|       <-V->F
    //   \         /
    //    `-------'
    //
    // L and m are unknown so find magnitude of vectors as the magnitude
    // of L is the radius of the sphere
    //
    // |L| = |F + mV - S| = r
    //
    // Can be rearranged to form a quadratic
    //
    // 0 = am&sup2; +bm + c
    //
    // and solved to find m, using the following formula
    //
    // <pre>
    //              ___________
    // m = ( -b &PlusMinus; \/(b&sup2;) - 4ac ) /2a
    // </pre>
    //
    // r = |F + mV - S|
    //       __________________________________________________
    // r = v(Fx + mVx -Sx)&sup2; + (Fy + mVy -Sy)&sup2; + (Fz + mVz -Sz)&sup2;
    //      
    // r&sup2; = (Fx + mVx -Sx)&sup2; + (Fy + mVy -Sy)&sup2; + (Fz + mVz -Sz)&sup2;
    //
    // r&sup2; = (Fx + mVx -Sx)&sup2; + (Fy + mVy -Sy)&sup2; + (Fz + mVz -Sz)&sup2;
    //
    // 0 = Fx&sup2; + FxVxm -FxSx + FxVxm + Vx&sup2;m&sup2; -SxVxm -SxFx -SxVxm + Sx&sup2;
    //    +Fy&sup2; + FyVym -FySy + FyVym + Vy&sup2;m&sup2; -SyVym -SyFy -SyVym + Sy&sup2;
    //    +Fz&sup2; + FzVzm -FzSz + FzVzm + Vz&sup2;m&sup2; -SzVzm -SzFz -SzVzm + Sz&sup2; - r&sup2;
    //    
    // 0 = Vx&sup2;m&sup2;          + FxVxm + FxVxm -2SxVxm    + Fx&sup2; -FxSx -SxFx + Sx&sup2;
    //    +Vy&sup2;m&sup2;          + FyVym + FyVym -2SyVym    + Fy&sup2; -FySy -SyFy + Sy&sup2;
    //    +Vz&sup2;m&sup2;          + FzVzm + FzVzm -2SzVzm    + Fz&sup2; -FzSz -SzFz + Sz&sup2; - r&sup2;
    //
    // 0 = (Vx&sup2; + Vy&sup2; + Vz&sup2;)m&sup2;  + (FxVx + FxVx -2SxVx)m    + Fx&sup2; - 2FxSx + Sx&sup2;
    //                          + (FyVy + FyVy -2SyVy)m    + Fy&sup2; - 2FySy + Sy&sup2;
    //                          + (FzVz + FzVz -2SzVz)m    + Fz&sup2; - 2FzSz + Sz&sup2; - r&sup2;
    //
    // 0 = |Vz|m&sup2;  + (FxVx + FxVx -2SxVx)m    + |F| - 2FxSx + |S|
    //             + (FyVy + FyVy -2SyVy)m          - 2FySy
    //             + (FyVy + FyVy -2SyVy)m          - 2FySy       - r&sup2;
    //
    // a = |Vz|
    // b = 
    // c = Fx&sup2; + Sx&sup2; -2FxSx + Fy&sup2; + Sy&sup2; -2FySy + Fz&sup2; + Sz&sup2; -2FzSz - r&sup2;
    // for current F and S this means c = Sy&sup2; - r&sup2;
    // </pre>

    // Where a, b and c are as in the code.
    // Only the solution for the negative square root term is needed as the
    // closest intersection is wanted. The other solution to m would give
    // the intersection of the 'back' of the sphere.

    this.a = this.V[this.X] * this.V[this.X] + this.VY2 + this.V[this.Z] * this.V[this.Z];

    this.s = (this.b2 - this.a * this.c4);    // the square root term

    // if s is negative then there are no solutions to m and the
    // sphere is not visible on the current pixel on the canvas
    // so only draw a pixel if the sphere is visable
    // 0 is a special case as it is the 'edge' of the sphere as there
    // is only one solution. (I have never seen it happen though)
    // of the two solutions m1 & m2 the nearest is m1, m2 being the
    // far side of the sphere.

    if (this.s > 0) {

      this.m1 = ((-this.b)-(Math.sqrt(this.s)))/(2*this.a);

      this.L[this.X]=this.m1*this.V[this.X];        //    bx+m1*V[X];
      this.L[this.Y]=this.by+(this.m1*this.V[this.Y]);
      this.L[this.Z]=this.m1*this.V[this.Z];        //    bz+m1*V[Z];

      // Do a couple of rotations on L

      this.lx=this.L[this.X];
      this.srz = Math.sin(this.rz);
      this.crz = Math.cos(this.rz);
      this.L[this.X]=this.lx*this.crz-this.L[this.Y]*this.srz;
      this.L[this.Y]=this.lx*this.srz+this.L[this.Y]*this.crz;

//      calcL(lx, L[Y], rz);

      this.lz=this.L[this.Z];
      this.sry = Math.sin(this.ry);
      this.cry = Math.cos(this.ry);
      this.L[this.Z]=this.lz*this.cry-this.L[this.Y]*this.sry;
      this.L[this.Y]=this.lz*this.sry+this.L[this.Y]*this.cry;

 //     calcL(lz, L[Y], ry);

      // Calculate the position that this location on the sphere
      // coresponds to on the texture

      var lh = this.textureWidth + this.textureWidth * (  Math.atan2(this.L[this.Y],this.L[this.X]) + Math.PI ) / (2*Math.PI);

      // %textureHeight at end to get rid of south pole bug. probaly means that one
      // pixel may be a color from the opposite pole but as long as the
      // poles are the same color this won't be noticed.

      var lv = this.textureWidth * Math.floor(this.textureHeight-1-(this.textureHeight*(Math.acos(this.L[this.Z]/this.r)/Math.PI)%this.textureHeight));
      return {lv:lv,lh:lh};
    }
    return null;
};

  
  /**
   * Create the sphere function opject
   */
Sphere.prototype.sphere = function() {

    var textureData = textureImageData.data;
    var canvasData = canvasImageData.data;

    var copyFnc;

    if (canvasData.splice){
      //2012-04-19 splice on canvas data not supported in any current browser
      copyFnc = function(idxC, idxT){
        canvasData.splice(idxC, 4  , textureData[idxT + 0]
                                  , textureData[idxT + 1]
                                  , textureData[idxT + 2]
                                  , 255);
      };
    } else {
      copyFnc = function(idxC, idxT){
        canvasData[idxC + 0] = textureData[idxT + 0];
        canvasData[idxC + 1] = textureData[idxT + 1];
        canvasData[idxC + 2] = textureData[idxT + 2];
        canvasData[idxC + 3] = 255;
      };
    }
    
    var getVector = (function(){
      var cache = new Array(size*size);
      return function(pixel){
        if (cache[pixel] === undefined){
          var v = Math.floor(pixel / size);
          var h = pixel - v * size;
          cache[pixel] = calculateVector(h,v);
        }
        return cache[pixel];
      };
    })();
    
    var posDelta = textureWidth/(20*1000);
    var firstFramePos = (new Date()) * posDelta;

    var stats = {fastCount: 0, fastSumMs: 0};

    return {
    
      renderFrame: function(time){
        this.RF(time);
        return;
        stats.firstMs = new Date() - time;
        this.renderFrame = this.sumRF;
        console.log(rotCache);
        for (var key in rotCache){
          if (rotCache[key] > 1){
            console.log(rotCache[key]);
          }
        }
      },
      sumRF: function(time){
        this.RF(time);
        stats.fastSumMs += new Date() - time;
        stats.fastCount++;
        if (stats.fastSumMs > stats.firstMs) {
   //       alert("calc:precompute ratio = 1:"+ stats.fastCount +" "+ stats.fastSumMs +" "+ stats.firstMs);
          this.renderFrame = this.RF;
        }
      },


    
      RF: function(time){
      // RX, RY & RZ may change part way through if the newR? (change tilt/turn) meathods are called while
      // this meathod is running so put them in temp vars at render start.
      // They also need converting from degrees to radians
      rx=RX*Math.PI/180;
      ry=RY*Math.PI/180;
      rz=RZ*Math.PI/180;

      // add to 24*60*60 so it will be a day before turnBy is negative and it hits the slow negative modulo bug
      var turnBy = 24*60*60 + firstFramePos - time * posDelta;
      var pixel = size*size;
      
      while(pixel--){
        var vector = getVector(pixel);
        if (vector !== null){
          //rotate texture on sphere
          var lh = Math.floor(vector.lh + turnBy) % textureWidth;
/*           lh = (lh < 0) 
                ? ((textureWidth-1) - ((lh-1)%textureWidth)) 
                : (lh % textureWidth) ;
 */
          var idxC = pixel * 4;
          var idxT = (lh + vector.lv) * 4;

          /* TODO light for alpha channel or alter s or l in hsl color value?
            - fn to calc distance between two points on sphere?
            - attenuate light by distance from point and rotate point separate from texture rotation
          */

          // Update the values of the pixel;
          canvasData[idxC + 0] = textureData[idxT + 0];
          canvasData[idxC + 1] = textureData[idxT + 1];
          canvasData[idxC + 2] = textureData[idxT + 2];
          canvasData[idxC + 3] = 255;

          // Slower?
          /*
          canvasImageData.data[idxC + 0] = textureImageData.data[idxT + 0];
          canvasImageData.data[idxC + 1] = textureImageData.data[idxT + 1];
          canvasImageData.data[idxC + 2] = textureImageData.data[idxT + 2];
          canvasImageData.data[idxC + 3] = 255;
          */
          // Faster?
          /* copyFnc(idxC,idxT); */
        }
      }
      gCtx.putImageData(canvasImageData, 0, 0);
  }};
};

Sphere.prototype.copyImageToBuffer = function () {
      this.gImage = document.createElement('canvas');
      this.textureWidth = this.img.naturalWidth;
      this.textureHeight = this.img.naturalHeight;
      this.gImage.width = this.textureWidth;
      this.gImage.height = this.textureHeight;

      this.gCtxImg = this.gImage.getContext("2d");
      this.gCtxImg.clearRect(0, 0, this.textureHeight, this.textureWidth);
      this.gCtxImg.drawImage(this.img, 0, 0);
      this.textureImageData = this.gCtxImg.getImageData(0, 0, this.textureHeight, this.textureWidth);

      this.hs_ch = (this.hs / this.size);
      this.vs_cv = (this.vs / this.size);
};

Sphere.prototype.drawImage = function() {
	//this.gCtx.fillStyle="#FF0000";
	//this.gCtx.fillRect(0,0,150,75);
	this.gCtx.drawImage(this.img, 0, 0, 100, 100);
};

var gSphere = null;

Sphere.prototype.draw = function () {
	this.size = Math.min(this.gCanvas.width, this.gCanvas.height);
	this.gCtx = this.gCanvas.getContext("2d");
	this.canvasImageData = this.gCtx.createImageData(this.size, this.size);

	this.ry=90+this.opts.tilt;
	this.rz=180+this.opts.turn;

	this.RY = (90-this.ry);
	this.RZ = (180-this.rz);

	this.hs_ch = (this.hs / this.size);
	this.vs_cv = (this.vs / this.size);

	this.V[this.Y]=this.f;

	this.b=(2*(-this.f*this.V[this.Y]));
	this.b2=Math.pow(this.b,2);
    this.img = new Image();
    
    gSphere = this;
    
    this.img.onload = 
    	function() {
    	gSphere.copyImageToBuffer();
    	var earth = sphere();
    	var renderAnimationFrame = function(time){
    		earth.renderFrame(time);
    		// window.requestAnimationFrame(renderAnimationFrame);
    	};

    	// Best! only renders frames that will be seen. stats.js runs at 60FPS on my desktop
    	window.requestAnimationFrame(renderAnimationFrame);
    	
    };
    this.img.setAttribute("src", this.textureUrl);
};


function Sphere2(canvas, textureUrl, tilt, period) {
	this.canvas = canvas;
	this.textureUrl = textureUrl;
	this.tilt = tilt;
	this.period = period;

	this.size = Math.min(this.canvas.width, this.canvas.height);
    this.gCtx = canvas.getContext("2d");
    this.canvasImageData = gCtx.createImageData(this.size, this.size);

    this.ry = 90 + this.tilt;
    this.rz = 180 + opts.turn;

    this.RY = (90-this.ry);
    this.RZ = (180-this.rz);

    this.hs_ch = (this.hs / this.size);
    this.vs_cv = (this.vs / this.size);

    this.V[Y]=f;

    b=(2*(-f*V[Y]));
    b2=Math.pow(b,2);

    this.img = new Image();
    this.img.setAttribute("src", textureUrl);
    
    this.img.onload = function() {
        copyImageToBuffer(this.img);
        var earth = sphere();
        var renderAnimationFrame = function(/* time */ time){
            /* time ~= +new Date // the unix time */
            earth.renderFrame(time);
        };

        // Best! only renders frames that will be seen. stats.js runs at 60FPS on my desktop
        window.requestAnimationFrame(renderAnimationFrame);
        img.setAttribute("src", textureUrl);

   };
};
