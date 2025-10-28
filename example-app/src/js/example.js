import { vestvalidator } from 'create-capacitor-plugin';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    vestvalidator.echo({ value: inputValue })
}
