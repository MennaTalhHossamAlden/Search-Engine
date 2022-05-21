const searchform = document.querySelector("#searching");
const searchinput = searchform.querySelector("input");
const mic = searchform.querySelector("button");
const micI = mic.querySelector("i");

const SpeechRecognition =
  window.SpeechRecognition || window.webkitSpeechRecognition;

const recognition = new SpeechRecognition();

// recognition.lang = "ar";

mic.addEventListener("click", micBtnClick);
function micBtnClick() {
  if (micI.classList.contains("fa-microphone")) {
    recognition.start();
  } else {
    recognition.stop();
  }
}

recognition.addEventListener("start", startspeech);
function startspeech() {
  micI.classList.remove("fa-microphone");
  micI.classList.add("fa-microphone-slash");

  console.log("active");
}
recognition.addEventListener("end", endspeech);
function endspeech() {
  micI.classList.remove("fa-microphone-slash");
  micI.classList.add("fa-microphone");
  searchinput.focus();
  console.log("deactive");
}

recognition.addEventListener("result", resulting);
function resulting(event) {
  const trans = event.results[0][0].transcript;
  searchinput.value = trans;
  setTimeout(()=>{searchform.submit();},750)
}
