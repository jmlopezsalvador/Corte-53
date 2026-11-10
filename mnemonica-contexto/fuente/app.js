// Lógica completa de la app (extraída de mnemonica.html, sin los audios en base64)
(function(){
  var SYM={C:'\u2663\uFE0E',H:'\u2665\uFE0E',D:'\u2666\uFE0E',P:'\u2660\uFE0E'};
  var SUIT={C:'tréboles',H:'corazones',D:'diamantes',P:'picas'};
  var RNAME={A:'As',J:'Jota',Q:'Reina',K:'Rey'};
  // Orden Mnemónica de Juan Tamariz (posición 1 a 52). T = 10, P = picas
  var STACK=['4C','2H','7D','3C','4H','6D','AP','5H','9P','2P','QH','3D','QC','8H','6P','5P','9H','KC','2D','JH',
             '3P','8P','6H','TC','5D','KD','2C','3H','8D','5C','KP','JD','8C','TP','KH','JC','7P','TH','AD','4P',
             '7H','4D','AC','9C','JP','QD','7C','QP','TD','6C','AH','9D'];
  var PIPS={
    '2':[[50,20],[50,80]],
    '3':[[50,20],[50,50],[50,80]],
    '4':[[30,20],[70,20],[30,80],[70,80]],
    '5':[[30,20],[70,20],[50,50],[30,80],[70,80]],
    '6':[[30,20],[70,20],[30,50],[70,50],[30,80],[70,80]],
    '7':[[30,20],[70,20],[50,35],[30,50],[70,50],[30,80],[70,80]],
    '8':[[30,20],[70,20],[50,35],[30,50],[70,50],[50,65],[30,80],[70,80]],
    '9':[[30,20],[70,20],[30,40],[70,40],[50,50],[30,60],[70,60],[30,80],[70,80]],
    '10':[[30,20],[70,20],[50,30],[30,40],[70,40],[30,60],[70,60],[50,70],[30,80],[70,80]]
  };

  function $(id){return document.getElementById(id);}
  function rk(c){return c[0]==='T'?'10':c[0];}
  function isRed(c){return c[1]==='H'||c[1]==='D';}
  function cname(c){var r=rk(c);return (RNAME[r]||r)+' de '+SUIT[c[1]];}
  function short(c){return rk(c)+SYM[c[1]];}

  function face(c){
    var r=rk(c), sym=SYM[c[1]], mid='';
    if(r==='A') mid='<div class="ace">'+sym+'</div>';
    else if('JQK'.indexOf(r)>=0) mid='<div class="court"><b>'+r+'</b><i>'+sym+'</i></div>';
    else mid=PIPS[r].map(function(p){
      return '<span class="pip'+(p[1]>50?' inv':'')+'" style="left:'+p[0]+'%;top:'+p[1]+'%">'+sym+'</span>';
    }).join('');
    var idx='<b>'+r+'</b><i>'+sym+'</i>';
    return '<div class="card'+(isRed(c)?' red':'')+'"><div class="idx tl">'+idx+'</div>'+mid+'<div class="idx br">'+idx+'</div></div>';
  }
  function backTile(pos){
    return '<div class="tile cardback"><div class="pat"></div><div class="num">'+pos+'</div></div>';
  }
  function withBadge(pos,c,hidden){
    return '<div class="big">'+face(c)+'<span class="bbadge'+(hidden?' hid':'')+'">'+pos+'</span></div>';
  }
  function shuffle(a){
    for(var i=a.length-1;i>0;i--){var j=Math.floor(Math.random()*(i+1));var t=a[i];a[i]=a[j];a[j]=t;}
    return a;
  }
  function show(id){
    var s=document.querySelectorAll('.screen');
    for(var i=0;i<s.length;i++) s[i].classList.toggle('on',s[i].id===id);
    window.scrollTo(0,0);
  }

  /* ---------- Ajustes ---------- */
  var cfg={order:'seq',dir:'n2c',from:1,to:52,filt:'all',suit:'P',rank:'8'};
  try{var saved=JSON.parse(localStorage.getItem('mnemo-cfg')||'null');if(saved){for(var k in saved)cfg[k]=saved[k];}}catch(e){}
  function save(){try{localStorage.setItem('mnemo-cfg',JSON.stringify(cfg));}catch(e){}}

  var opts='';
  for(var n=1;n<=52;n++) opts+='<option value="'+n+'">'+n+'</option>';
  $('from').innerHTML=opts;$('to').innerHTML=opts;

  var ORDER_HINT={seq:'Una detrás de otra, de la primera posición a la última.',
                  rnd:'Todas las del rango, barajadas y sin repetir.'};
  var DIR_HINT={n2c:'Te muestro el número y tú dices la carta.',
                c2n:'Te muestro la carta y tú dices su número.',
                mix:'A veces número, a veces carta, al azar.'};

  function syncSetup(){
    var segs=document.querySelectorAll('.seg[data-key]');
    for(var i=0;i<segs.length;i++){
      var b=segs[i].querySelectorAll('button');
      for(var j=0;j<b.length;j++) b[j].classList.toggle('sel',b[j].getAttribute('data-val')===cfg[segs[i].getAttribute('data-key')]);
    }
    $('from').value=cfg.from;$('to').value=cfg.to;
    var ch=document.querySelectorAll('.chip[data-f]');
    for(var k=0;k<ch.length;k++) ch[k].classList.toggle('sel',+ch[k].getAttribute('data-f')===cfg.from&&+ch[k].getAttribute('data-t')===cfg.to);
    $('orderHint').textContent=ORDER_HINT[cfg.order];
    $('dirHint').textContent=DIR_HINT[cfg.dir];
    $('suitChips').hidden=cfg.filt!=='suit';$('rankChips').hidden=cfg.filt!=='rank';
    var sc=document.querySelectorAll('[data-suit]');for(var a=0;a<sc.length;a++)sc[a].classList.toggle('sel',sc[a].getAttribute('data-suit')===cfg.suit);
    var rc=document.querySelectorAll('[data-rank]');for(var r=0;r<rc.length;r++)rc[r].classList.toggle('sel',rc[r].getAttribute('data-rank')===cfg.rank);
    var n=positions().length;
    $('filtHint').textContent=cfg.filt==='all'?'Todas las cartas del rango.':
      (n?'Posiciones: '+positions().join(', ')+'.':'Ninguna carta con ese filtro en el rango elegido.');
    $('startBtn').textContent=n?'Empezar · '+n+(n===1?' carta':' cartas'):'Sin cartas que entrenar';
  }
  var segEls=document.querySelectorAll('.seg[data-key]');
  for(var s=0;s<segEls.length;s++){
    segEls[s].addEventListener('click',function(e){
      var b=e.target.closest('button');if(!b)return;
      cfg[this.getAttribute('data-key')]=b.getAttribute('data-val');save();syncSetup();
    });
  }
  function onRange(){
    var f=+$('from').value,t=+$('to').value;
    if(f>t){var x=f;f=t;t=x;}
    cfg.from=f;cfg.to=t;save();syncSetup();
  }
  $('from').addEventListener('change',onRange);$('to').addEventListener('change',onRange);
  var chips=document.querySelectorAll('.chip[data-f]');
  for(var c=0;c<chips.length;c++){
    chips[c].addEventListener('click',function(){
      cfg.from=+this.getAttribute('data-f');cfg.to=+this.getAttribute('data-t');save();syncSetup();
    });
  }

  var SUITW={P:'picas',H:'corazones',C:'tréboles',D:'diamantes'};
  function filtLabel(){
    if(cfg.filt==='suit')return 'solo '+SUITW[cfg.suit];
    if(cfg.filt==='rank')return 'solo '+(cfg.rank==='T'?'10':cfg.rank)+'s';
    return '';
  }
  function positions(){
    var a=[];
    for(var i=cfg.from;i<=cfg.to;i++){
      var c=STACK[i-1];
      if(cfg.filt==='suit'&&c[1]!==cfg.suit)continue;
      if(cfg.filt==='rank'&&c[0]!==cfg.rank)continue;
      a.push(i);
    }
    return a;
  }
  document.querySelector('#suitChips').addEventListener('click',function(e){
    var b=e.target.closest('[data-suit]');if(!b)return;cfg.suit=b.getAttribute('data-suit');save();syncSetup();
  });
  document.querySelector('#rankChips').addEventListener('click',function(e){
    var b=e.target.closest('[data-rank]');if(!b)return;cfg.rank=b.getAttribute('data-rank');save();syncSetup();
  });

  /* ---------- Sesión ---------- */
  var ses=null,lastFails=[];
  function buildList(){
    var a=positions();
    return cfg.order==='rnd'?shuffle(a):a;
  }
  function start(list){
    if(!list.length)return;
    ses={list:list,i:0,res:list.map(function(){return null;}),flipped:false,revealed:false,t0:Date.now(),
         dirs:list.map(function(){return cfg.dir==='mix'?(Math.random()<.5?'n2c':'c2n'):cfg.dir;})};
    show('train');render();
  }
  function counts(){
    var ok=0,ko=0;
    for(var k=0;k<ses.res.length;k++){if(ses.res[k]===true)ok++;else if(ses.res[k]===false)ko++;}
    return {ok:ok,ko:ko};
  }
  function markInfo(){
    var r=ses.res[ses.i];
    $('bOk').classList.toggle('chosen',r===true);$('bKo').classList.toggle('chosen',r===false);
    return r===true?' · marcada ✓':(r===false?' · marcada ✗':'');
  }
  function render(){
    var flip=$('flip'),pos=ses.list[ses.i],card=STACK[pos-1],d=ses.dirs[ses.i];
    flip.classList.add('noanim');flip.classList.remove('flipped');
    ses.flipped=false;ses.revealed=false;
    $('front').innerHTML=d==='n2c'?backTile(pos):withBadge(pos,card,true);
    $('back').innerHTML=d==='n2c'?withBadge(pos,card,false):'';
    void flip.offsetWidth;
    flip.classList.remove('noanim');
    $('prog').textContent=(ses.i+1)+' / '+ses.list.length;
    $('fill').style.width=(ses.i/ses.list.length*100)+'%';
    var c=counts();$('sOk').textContent='✓ '+c.ok;$('sKo').textContent='✗ '+c.ko;
    $('tip').textContent=(d==='n2c'?'¿Qué carta está en esta posición?':'¿En qué posición está esta carta?')+markInfo();
    $('actions').classList.remove('on');
    $('bPrev').disabled=ses.i===0;
    $('bNext').textContent=ses.i===ses.list.length-1?'Terminar ›':'Siguiente ›';
  }
  function toggle(){
    if(!ses)return;
    ses.flipped=!ses.flipped;
    if(ses.dirs[ses.i]==='c2n'){
      var bd=$('front').querySelector('.bbadge');if(bd)bd.classList.toggle('hid',!ses.flipped);
    }else{
      $('flip').classList.toggle('flipped',ses.flipped);
    }
    if(ses.flipped&&!ses.revealed){
      ses.revealed=true;$('actions').classList.add('on');
      $('tip').textContent='¿Lo sabías?'+markInfo();
    }
  }
  function go(step){
    if(!ses)return;
    var n=ses.i+step;
    if(n<0)return;
    if(n>=ses.list.length){finish();return;}
    ses.i=n;render();
  }
  function answer(ok){
    if(!ses||!ses.revealed)return;
    ses.res[ses.i]=ok;
    go(1);
  }
  function fmt(sec){var m=Math.floor(sec/60),s=sec%60;return m+':'+(s<10?'0':'')+s;}
  function finish(){
    var total=ses.list.length,c=counts(),marked=c.ok+c.ko,skipped=total-marked,secs=Math.round((Date.now()-ses.t0)/1000);
    if(marked){
      $('rScore').textContent=c.ok+' / '+marked;
      $('rSub').textContent=Math.round(c.ok/marked*100)+'% de aciertos · '+fmt(secs)+' min'+(skipped?' · '+skipped+' sin marcar':'');
    }else{
      $('rScore').textContent=total+(total===1?' carta':' cartas');
      $('rSub').textContent='Recorrido sin marcar · '+fmt(secs)+' min';
    }
    var ko=[];for(var k=0;k<total;k++)if(ses.res[k]===false)ko.push(ses.list[k]);
    lastFails=ko;
    var sorted=lastFails.slice().sort(function(a,b){return a-b;});
    if(sorted.length){
      $('rFailsLabel').hidden=false;
      $('rFails').innerHTML=sorted.map(function(p){var cd=STACK[p-1];
        return '<span class="fchip'+(isRed(cd)?' red':'')+'"><b>'+p+'</b>'+short(cd)+'</span>';}).join('');
      $('retryFails').hidden=false;
    }else{
      $('rFailsLabel').hidden=true;
      $('rFails').innerHTML=marked?'<p class="hint" style="font-size:1.1rem">¡Sin fallos! 🎩</p>':'';
      $('retryFails').hidden=true;
    }
    show('end');
  }

  $('flip').addEventListener('click',toggle);
  $('bOk').addEventListener('click',function(){answer(true);});
  $('bKo').addEventListener('click',function(){answer(false);});
  $('bPrev').addEventListener('click',function(){go(-1);});
  $('bNext').addEventListener('click',function(){go(1);});
  (function(){
    var st=document.querySelector('#train .stage'),x0=null,y0=0,swiped=0;
    st.addEventListener('touchstart',function(e){if(e.touches.length!==1){x0=null;return;}x0=e.touches[0].clientX;y0=e.touches[0].clientY;},{passive:true});
    st.addEventListener('touchend',function(e){
      if(x0===null)return;
      var t=e.changedTouches[0],dx=t.clientX-x0,dy=t.clientY-y0;x0=null;
      if(Math.abs(dx)>50&&Math.abs(dx)>Math.abs(dy)*1.5){swiped=Date.now();go(dx<0?1:-1);}
    });
    $('flip').addEventListener('click',function(e){if(Date.now()-swiped<500)e.stopImmediatePropagation();},true);
  })();
  $('startBtn').addEventListener('click',function(){start(buildList());});
  $('quit').addEventListener('click',function(){ses=null;show('setup');});
  $('again').addEventListener('click',function(){start(buildList());});
  $('toMenu').addEventListener('click',function(){show('setup');});
  $('retryFails').addEventListener('click',function(){
    var l=lastFails.slice();
    start(cfg.order==='rnd'?shuffle(l):l.sort(function(a,b){return a-b;}));
  });

  /* ---------- Baraja ---------- */
  $('grid').innerHTML=STACK.map(function(cd,i){return '<div class="gcell">'+face(cd)+'<span class="badge">'+(i+1)+'</span></div>';}).join('');
  $('deckBtn').addEventListener('click',function(){show('deck');});
  $('deckBack').addEventListener('click',function(){show('setup');});
  $('glossBtn').addEventListener('click',function(){show('gloss');});
  $('glossBack').addEventListener('click',function(){show('setup');});

  /* ---------- Cortes ---------- */
  var C={n:0,p:0,t:0,shown:false};
  function cNew(){
    var pool=positions();
    if(!pool.length){pool=[];for(var k=1;k<=52;k++)pool.push(k);}
    var p=pool[Math.floor(Math.random()*pool.length)],t;
    do{t=1+Math.floor(Math.random()*52);}while(t===p);
    C.p=p;C.t=t;C.n++;C.shown=false;
    $('cCard').innerHTML=withBadge(p,STACK[p-1],true);
    $('cTo').textContent=t;
    $('cNum').textContent='Nº '+C.n;
    $('cAsk').hidden=false;$('cSol').hidden=true;
    kLabel();
  }
  function cs(c){return isRed(c)?'<span style="color:#ff9a8f">'+short(c)+'</span>':short(c);}
  function cReveal(){
    var p=C.p,t=C.t,c=STACK[p-1],steps=[],q,up;
    steps.push('El '+cs(c)+' está en la '+p+': <b>C = '+p+'</b>.');
    if(t>p){
      var d=t-p;q=53-d;up=true;
      steps.push('Destino <b>D = '+t+'</b>. D &gt; C: <i>tienes que bajar la carta</i>.');
      steps.push('D − C = '+t+' − '+p+' = <b>'+d+'</b>.');
      steps.push('N = 53 − (D − C) = 53 − '+d+' = <b>'+q+'</b>.');
    }else{
      q=p-t;up=false;
      steps.push('Destino <b>D = '+t+'</b>. D &lt; C: <i>tienes que subir la carta</i>.');
      steps.push('N = C − D = '+p+' − '+t+' = <b>'+q+'</b>.');
    }
    var cc=STACK[q-1];C.q=q;C.up=up;C.cc=cc;
    steps.push('En N = '+q+' está el '+cs(cc)+'.');
    $('cSteps').innerHTML=steps.map(function(x){return '<li>'+x+'</li>';}).join('');
    $('cCutCard').innerHTML=withBadge(q,cc,false);
    var dir=$('cDir');dir.textContent=up?'↑ Cortar arriba':'↓ Cortar abajo';dir.className='cDir '+(up?'up':'down');
    var moved=up?q-1:q;
    $('cNote').textContent=(up?'El '+cname(cc)+' queda arriba del todo':'El '+cname(cc)+' queda abajo del todo')+
      ' (pasas '+moved+(moved===1?' carta':' cartas')+' de arriba abajo).';
    var bd=$('cCard').querySelector('.bbadge');if(bd)bd.classList.remove('hid');
    $('cAsk').hidden=true;$('cSol').hidden=false;
    C.shown=true;kLabel();
  }
  /* voz de cortes */
  var K={mode:'manual',think:5,step:0.75,next:2,rate:1,on:false,tok:0};
  try{var ks=JSON.parse(localStorage.getItem('mnemo-cortes')||'null');if(ks){['mode','think','step','next','rate'].forEach(function(k){if(k in ks)K[k]=ks[k];});}}catch(e){}
  function kSave(){try{localStorage.setItem('mnemo-cortes',JSON.stringify({mode:K.mode,think:K.think,step:K.step,next:K.next,rate:K.rate}));}catch(e){}}
  function kLabel(){
    $('cBtn').textContent=K.mode==='voice'?(K.on?'❚❚ Pausa':'▶ Cantar'):(C.shown?'Siguiente ›':'Revelar');
  }
  function kSync(){
    var b=document.querySelectorAll('.kseg button');
    for(var i=0;i<b.length;i++)b[i].classList.toggle('sel',b[i].getAttribute('data-val')===K.mode);
    var v=K.mode==='voice';$('cVoice').hidden=!v;$('cNext').hidden=!v;
    $('kThink').value=K.think;$('kThinkOut').textContent=dec(K.think)+' s';
    $('kStep').value=K.step;$('kStepOut').textContent=K.step.toLocaleString('es-ES',{maximumFractionDigits:2})+' s';
    $('kNext').value=K.next;$('kNextOut').textContent=dec(K.next)+' s';
    $('kRate').value=K.rate;$('kRateOut').textContent=dec(K.rate)+'×';
    kLabel();
  }
  function kSay(key,cb){
    var t=K.tok,done=false;
    function fin(){if(done)return;done=true;if(t===K.tok&&K.on)cb();}
    player.onended=fin;player.onerror=fin;player.onplaying=null;
    player.src=CLIPS[key];player.defaultPlaybackRate=K.rate;player.playbackRate=K.rate;
    var p=player.play();
    if(p&&p.catch)p.catch(function(e){
      if(t!==K.tok)return;kHalt();kSync();
      var n=$('cNotice');n.textContent='El navegador no ha dejado reproducir el audio ('+(e&&e.name||'error')+'). Toca Cantar otra vez y sube el volumen.';n.hidden=false;
    });
    setTimeout(fin,6000/K.rate);
  }
  function kWait(sec,cb){var t=K.tok;setTimeout(function(){if(t===K.tok&&K.on)cb();},sec*1000);}
  function kSeq(items,cb){
    var i=0;
    (function step(){
      if(i>=items.length){cb();return;}
      var it=items[i++];
      if(typeof it==='number')kWait(it,step);
      else kSay(it,function(){kWait(.05,step);});
    })();
  }
  function kCycle(){
    var p=C.p,t=C.t;
    kSeq(['c'+STACK[p-1],'kAla','n'+t],function(){
      kWait(K.think,function(){
        cReveal();
        var S=K.step,q=C.q,d=C.up?t-p:p-t,seq=['kEsta','n'+p,S];
        if(C.up)seq=seq.concat(['n'+t,'minus','n'+p,'kEs','n'+d,S,'n53','minus','n'+d,'kEs','n'+q,S]);
        else seq=seq.concat(['n'+p,'minus','n'+t,'kEs','n'+d,S]);
        seq=seq.concat(['c'+C.cc,'kEnla','n'+q,S,C.up?'kUp':'kDown']);
        kSeq(seq,function(){kWait(K.next,function(){cNew();kCycle();});});
      });
    });
  }
  function kHalt(){K.on=false;K.tok++;try{player.pause();}catch(e){}if(wl){try{wl.release();}catch(e){}wl=null;}}
  function kStart(){
    $('cNotice').hidden=true;K.on=true;K.tok++;
    try{if(navigator.wakeLock)navigator.wakeLock.request('screen').then(function(l){wl=l;},function(){});}catch(e){}
    if(C.shown)cNew();
    kLabel();kCycle();
  }
  function cAct(){
    if(K.mode==='voice'){if(K.on){kHalt();kLabel();}else kStart();return;}
    if(C.shown)cNew();else cReveal();
  }
  function cSkip(){
    if(K.mode==='voice'){var was=K.on;kHalt();cNew();if(was)kStart();else kLabel();return;}
    cAct();
  }
  $('cBtn').addEventListener('click',cAct);
  $('cNext').addEventListener('click',cSkip);
  $('cPrompt').addEventListener('click',function(){if(K.mode==='manual'&&!C.shown)cReveal();});
  document.querySelector('.kseg').addEventListener('click',function(e){
    var b=e.target.closest('button');if(!b)return;
    kHalt();K.mode=b.getAttribute('data-val');kSave();kSync();
  });
  $('kThink').addEventListener('input',function(){K.think=+this.value;kSave();kSync();});
  $('kStep').addEventListener('input',function(){K.step=+this.value;kSave();kSync();});
  $('kNext').addEventListener('input',function(){K.next=+this.value;kSave();kSync();});
  $('kRate').addEventListener('input',function(){K.rate=+this.value;kSave();kSync();});
  $('cutsBtn').addEventListener('click',function(){kHalt();C.n=0;show('cuts');cNew();kSync();});
  $('cBack').addEventListener('click',function(){kHalt();kSync();show('setup');});
  (function(){
    var sc=$('cuts'),x0=null,y0=0;
    sc.addEventListener('touchstart',function(e){if(e.touches.length!==1){x0=null;return;}x0=e.touches[0].clientX;y0=e.touches[0].clientY;},{passive:true});
    sc.addEventListener('touchend',function(e){
      if(x0===null)return;var t=e.changedTouches[0],dx=t.clientX-x0,dy=t.clientY-y0;x0=null;
      if(dx<-50&&Math.abs(dx)>Math.abs(dy)*1.5)cSkip();
    });
  })();

  /* ---------- Audio ---------- */
  var A={on:false,heard:false,lastErr:'',list:[],i:0,tok:0,mode:'say',gap:1.5,inner:0.5,rate:1,loop:false,show:true},wl=null;
  try{var as=JSON.parse(localStorage.getItem('mnemo-audio')||'null');if(as){['mode','gap','inner','rate','loop','show'].forEach(function(k){if(k in as)A[k]=as[k];});}}catch(e){}
  function aSave(){try{localStorage.setItem('mnemo-audio',JSON.stringify({mode:A.mode,gap:A.gap,inner:A.inner,rate:A.rate,loop:A.loop,show:A.show}));}catch(e){}}
  var player=new Audio();player.preload='auto';
  try{player.preservesPitch=true;player.webkitPreservesPitch=true;}catch(e){}
  function notice(msg){var n=$('aNoVoice');n.textContent=msg;n.hidden=false;}
  var A_HINT={say:'Dice el número y la carta seguidos: «uno, cuatro de tréboles».',
              n2c:'Dice el número y, tras la pausa, la carta.',
              c2n:'Dice la carta y, tras la pausa, el número.',
              onum:'Lee solo los números. Tú dices la carta en la pausa.',
              ocard:'Lee solo las cartas. Tú dices el número en la pausa.'};
  function dec(n){return n.toLocaleString('es-ES',{minimumFractionDigits:1,maximumFractionDigits:1});}
  function aSync(){
    var b=document.querySelectorAll('.aseg button');
    for(var i=0;i<b.length;i++)b[i].classList.toggle('sel',b[i].getAttribute('data-val')===A.mode);
    $('aModeHint').textContent=A_HINT[A.mode];
    $('aInner').value=A.inner;$('aInnerOut').textContent=dec(A.inner)+' s';
    $('aInnerBox').hidden=(A.mode==='onum'||A.mode==='ocard');
    $('aGap').value=A.gap;$('aGapOut').textContent=dec(A.gap)+' s';
    $('aRate').value=A.rate;$('aRateOut').textContent=dec(A.rate)+'×';
    $('aLoop').checked=A.loop;$('aShow').checked=A.show;
    $('aStageWrap').classList.toggle('off',!A.show);
    $('aInfo').textContent='Posiciones '+cfg.from+'–'+cfg.to+' · '+(cfg.order==='rnd'?'aleatorio':'secuencial')+(cfg.filt!=='all'?' · '+filtLabel():'');
    $('aPlay').textContent=A.on?'❚❚ Pausa':(A.i>0&&A.i<A.list.length?'▶ Continuar':'▶ Reproducir');
  }
  function aShowItem(html){$('aStage').innerHTML=html;}
  function aProg(){$('aProg').textContent=Math.min(A.i+1,A.list.length)+' / '+A.list.length;}
  function speak(key,cb){
    var t=A.tok,done=false;
    function fin(){if(done)return;done=true;if(t===A.tok&&A.on)cb();}
    player.onended=fin;
    player.onplaying=function(){A.heard=true;$('aNoVoice').hidden=true;};
    player.onerror=function(){A.lastErr='error';fin();};
    player.src=CLIPS[key];
    player.defaultPlaybackRate=A.rate;player.playbackRate=A.rate;
    var p=player.play();
    if(p&&p.catch)p.catch(function(e){
      if(t!==A.tok)return;
      aHalt();aSync();
      notice('El navegador no ha dejado reproducir el audio ('+(e&&e.name||'error')+'). Toca Reproducir otra vez y sube el volumen.');
    });
    setTimeout(fin,6000/A.rate);
  }
  function wait(sec,cb){var t=A.tok;setTimeout(function(){if(t===A.tok&&A.on)cb();},sec*1000);}
  function aNext(){A.i++;aStep();}
  function aStep(){
    if(A.i>=A.list.length){
      if(A.loop){A.list=buildList();A.i=0;}
      else{aHalt();A.i=A.list.length;aSync();$('aPlay').textContent='▶ Otra vez';return;}
    }
    aProg();
    var pos=A.list[A.i],c=STACK[pos-1];
    if(A.mode==='say'){
      aShowItem(withBadge(pos,c,false));
      speak('n'+pos,function(){wait(A.inner,function(){speak('c'+c,function(){wait(A.gap,aNext);});});});
    }else if(A.mode==='onum'){
      aShowItem(backTile(pos));
      speak('n'+pos,function(){wait(A.gap,aNext);});
    }else if(A.mode==='ocard'){
      aShowItem(withBadge(pos,c,true));
      speak('c'+c,function(){wait(A.gap,aNext);});
    }else if(A.mode==='n2c'){
      aShowItem(backTile(pos));
      speak('n'+pos,function(){wait(A.inner,function(){
        aShowItem(withBadge(pos,c,false));
        speak('c'+c,function(){wait(A.gap,aNext);});
      });});
    }else{
      aShowItem(withBadge(pos,c,true));
      speak('c'+c,function(){wait(A.inner,function(){
        var bd=$('aStage').querySelector('.bbadge');if(bd)bd.classList.remove('hid');
        speak('n'+pos,function(){wait(A.gap,aNext);});
      });});
    }
  }
  function aHalt(){
    A.on=false;A.tok++;
    try{player.pause();}catch(e){}
    if(wl){try{wl.release();}catch(e){}wl=null;}
  }
  function aReset(){
    aHalt();A.list=buildList();A.i=0;aProg();
    if(!A.list.length){aShowItem('');aSync();notice('No hay cartas con ese filtro en el rango elegido. Cámbialo en el menú.');return;}
    var pos=A.list[0];aShowItem(withBadge(pos,STACK[pos-1],false));aSync();
  }
  $('aPlay').addEventListener('click',function(){
    $('aNoVoice').hidden=true;
    if(A.on){aHalt();aSync();return;}
    if(!A.list.length||A.i>=A.list.length){A.list=buildList();A.i=0;}
    if(!A.list.length){notice('No hay cartas con ese filtro en el rango elegido. Cámbialo en el menú.');return;}
    A.on=true;A.tok++;
    try{if(navigator.wakeLock)navigator.wakeLock.request('screen').then(function(l){wl=l;},function(){});}catch(e){}
    aSync();aStep();
  });
  $('aReset').addEventListener('click',aReset);
  document.querySelector('.aseg').addEventListener('click',function(e){
    var b=e.target.closest('button');if(!b)return;
    A.mode=b.getAttribute('data-val');aSave();
    if(A.on){aHalt();}aSync();
  });
  $('aInner').addEventListener('input',function(){A.inner=+this.value;aSave();aSync();});
  $('aGap').addEventListener('input',function(){A.gap=+this.value;aSave();aSync();});
  $('aRate').addEventListener('input',function(){A.rate=+this.value;aSave();aSync();});
  $('aLoop').addEventListener('change',function(){A.loop=this.checked;aSave();});
  $('aShow').addEventListener('change',function(){A.show=this.checked;aSave();aSync();});
  $('audioBtn').addEventListener('click',function(){
    show('audio');aReset();
  });
  $('aBack').addEventListener('click',function(){aHalt();show('setup');});

  /* ---------- Restas ---------- */
  var R={mode:'diff',think:3,gap:1.5,rate:1,on:false,tok:0,n:0,a:0,b:0,res:0,shown:false};
  try{var rs=JSON.parse(localStorage.getItem('mnemo-restas')||'null');if(rs){['mode','think','gap','rate'].forEach(function(k){if(k in rs)R[k]=rs[k];});}}catch(e){}
  function rSave(){try{localStorage.setItem('mnemo-restas',JSON.stringify({mode:R.mode,think:R.think,gap:R.gap,rate:R.rate}));}catch(e){}}
  function rSync(){
    var b=document.querySelectorAll('.rseg button');
    for(var i=0;i<b.length;i++)b[i].classList.toggle('sel',b[i].getAttribute('data-val')===R.mode);
    $('rThink').value=R.think;$('rThinkOut').textContent=dec(R.think)+' s';
    $('rGap').value=R.gap;$('rGapOut').textContent=dec(R.gap)+' s';
    $('rRate').value=R.rate;$('rRateOut').textContent=dec(R.rate)+'×';
    $('rPlay').textContent=R.on?'❚❚ Pausa':'▶ Cantar';
  }
  function rNew(){
    var x=1+Math.floor(Math.random()*52),y;
    if(R.mode==='c53'){R.a=53;R.b=x;}
    else{do{y=1+Math.floor(Math.random()*52);}while(y===x);R.a=Math.max(x,y);R.b=Math.min(x,y);}
    R.res=R.a-R.b;R.n++;R.shown=false;
    $('rA').textContent=R.a;$('rB').textContent=R.b;
    var r=$('rR');r.textContent='?';r.classList.add('q');
    $('rNum').textContent='Nº '+R.n;
    $('rTip').textContent=R.on?'':'Toca para ver el resultado';
  }
  function rShow(){
    R.shown=true;var r=$('rR');r.textContent=R.res;r.classList.remove('q');
    $('rTip').textContent=R.on?'':'Toca para la siguiente';
  }
  function rSay(key,cb){
    var t=R.tok,done=false;
    function fin(){if(done)return;done=true;if(t===R.tok&&R.on)cb();}
    player.onended=fin;player.onerror=fin;player.onplaying=null;
    player.src=CLIPS[key];player.defaultPlaybackRate=R.rate;player.playbackRate=R.rate;
    var p=player.play();
    if(p&&p.catch)p.catch(function(e){
      if(t!==R.tok)return;rHalt();rSync();
      var n=$('rNotice');n.textContent='El navegador no ha dejado reproducir el audio ('+(e&&e.name||'error')+'). Toca Cantar otra vez y sube el volumen.';n.hidden=false;
    });
    setTimeout(fin,6000/R.rate);
  }
  function rWait(sec,cb){var t=R.tok;setTimeout(function(){if(t===R.tok&&R.on)cb();},sec*1000);}
  function rCycle(){
    rSay('n'+R.a,function(){rWait(.05,function(){rSay('minus',function(){rWait(.05,function(){rSay('n'+R.b,function(){
      rWait(R.think,function(){rShow();rSay('n'+R.res,function(){rWait(R.gap,function(){rNew();rCycle();});});});
    });});});});});
  }
  function rHalt(){R.on=false;R.tok++;try{player.pause();}catch(e){}if(wl){try{wl.release();}catch(e){}wl=null;}}
  $('rPlay').addEventListener('click',function(){
    if(R.on){rHalt();rSync();$('rTip').textContent=R.shown?'Toca para la siguiente':'Toca para ver el resultado';return;}
    $('rNotice').hidden=true;R.on=true;R.tok++;
    try{if(navigator.wakeLock)navigator.wakeLock.request('screen').then(function(l){wl=l;},function(){});}catch(e){}
    if(R.shown)rNew();
    $('rTip').textContent='';rSync();rCycle();
  });
  function rManual(){if(R.on)return;if(R.shown)rNew();else rShow();}
  $('rBoard').addEventListener('click',rManual);
  $('rNext').addEventListener('click',function(){var was=R.on;rHalt();rNew();if(was){R.on=true;R.tok++;rCycle();}rSync();});
  document.querySelector('.rseg').addEventListener('click',function(e){
    var b=e.target.closest('button');if(!b)return;
    R.mode=b.getAttribute('data-val');rSave();
    var was=R.on;rHalt();rNew();if(was){R.on=true;R.tok++;rCycle();}rSync();
  });
  $('rThink').addEventListener('input',function(){R.think=+this.value;rSave();rSync();});
  $('rGap').addEventListener('input',function(){R.gap=+this.value;rSave();rSync();});
  $('rRate').addEventListener('input',function(){R.rate=+this.value;rSave();rSync();});
  $('subsBtn').addEventListener('click',function(){R.n=0;show('subs');rNew();rSync();});
  $('rBack').addEventListener('click',function(){rHalt();rSync();show('setup');});

  /* Teclado (por si se usa en ordenador): espacio = girar, flechas = avanzar/retroceder, A = acierto, F = fallo */
  document.addEventListener('keydown',function(e){
    if($('cuts').classList.contains('on')){
      if(e.key===' '||e.key==='Enter'||e.key==='ArrowRight'){e.preventDefault();cAct();}
      return;
    }
    if(!$('train').classList.contains('on'))return;
    if(e.key===' '||e.key==='Enter'){e.preventDefault();toggle();}
    else if(e.key==='ArrowRight')go(1);
    else if(e.key==='ArrowLeft')go(-1);
    else if(e.key==='a'||e.key==='A')answer(true);
    else if(e.key==='f'||e.key==='F')answer(false);
  });

  syncSetup();
})();
