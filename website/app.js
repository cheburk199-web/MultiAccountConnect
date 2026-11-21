import { initializeApp } from "https://www.gstatic.com/firebasejs/10.14.1/firebase-app.js";
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.14.1/firebase-auth.js";
import { getFirestore, doc, getDoc, setDoc } from "https://www.gstatic.com/firebasejs/10.14.1/firebase-firestore.js";

// Заполни эти значения из Firebase Console -> Project settings -> Your apps.
const firebaseConfig = {
  apiKey: "PASTE_API_KEY",
  authDomain: "PASTE_PROJECT_ID.firebaseapp.com",
  projectId: "PASTE_PROJECT_ID",
  appId: "PASTE_APP_ID"
};
const configured = !Object.values(firebaseConfig).some(v => v.startsWith("PASTE_"));
const authPanel = document.querySelector("#auth"), dashboard = document.querySelector("#dashboard");
const message = document.querySelector("#authMessage"), saveMessage = document.querySelector("#saveMessage");
let auth, db, currentUser, slotsData = Array.from({length:5},(_,i)=>({name:"Аккаунт "+(i+1),nickname:""}));
if(configured){const app=initializeApp(firebaseConfig);auth=getAuth(app);db=getFirestore(app);onAuthStateChanged(auth, async user=>{currentUser=user;authPanel.classList.toggle("hidden",!!user);dashboard.classList.toggle("hidden",!user);if(user){document.querySelector("#userEmail").textContent=user.email;await loadSlots();}});}else{message.textContent="Сайт подготовлен. Для включения регистрации владелец должен подключить проект Firebase.";}
async function loadSlots(){const snap=await getDoc(doc(db,"profiles",currentUser.uid));if(snap.exists()&&Array.isArray(snap.data().slots))slotsData=snap.data().slots.slice(0,5);renderSlots();}
function renderSlots(){const root=document.querySelector("#slots");root.innerHTML="";for(let i=0;i<5;i++){const s=slotsData[i]||{name:"Аккаунт "+(i+1),nickname:""};const card=document.createElement("div");card.className="slot";const title=document.createElement("h3");title.textContent="Слот "+(i+1);const name=document.createElement("input");name.maxLength=32;name.placeholder="Название профиля";name.value=s.name||"";const nick=document.createElement("input");nick.maxLength=32;nick.placeholder="Minecraft-ник (не пароль)";nick.value=s.nickname||"";name.addEventListener("input",()=>{slotsData[i].name=name.value});nick.addEventListener("input",()=>{slotsData[i].nickname=nick.value});const save=document.createElement("button");save.textContent="Сохранить слот";save.addEventListener("click",saveSlots);card.append(title,name,nick,save);root.append(card);}}
async function saveSlots(){try{await setDoc(doc(db,"profiles",currentUser.uid),{slots:slotsData.map((s,i)=>({name:String(s.name||"").slice(0,32),nickname:String(s.nickname||"").slice(0,32)}))});saveMessage.textContent="Слоты сохранены в твоём профиле."; }catch(e){saveMessage.textContent="Не удалось сохранить: "+e.message;}}
document.querySelector("#authForm").addEventListener("submit",async e=>{e.preventDefault();if(!configured)return;try{await signInWithEmailAndPassword(auth,document.querySelector("#email").value,document.querySelector("#password").value);message.textContent="";}catch(err){message.textContent=err.message;}});
document.querySelector("#register").addEventListener("click",async()=>{if(!configured)return;try{await createUserWithEmailAndPassword(auth,document.querySelector("#email").value,document.querySelector("#password").value);message.textContent="Аккаунт создан.";}catch(err){message.textContent=err.message;}});
document.querySelector("#logout").addEventListener("click",()=>signOut(auth));
document.querySelector("#launch").addEventListener("click",()=>{location.href="intent://launch/#Intent;package=git.artdeell.mjlaunch;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;end";});
