package com.example.calculadorafernando

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.Expression
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var editResult: EditText
    private var expression = StringBuilder()
    private var isInverseMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = android.graphics.Color.parseColor("#3B3936")

        editResult = findViewById(R.id.textResult)
        editResult.isSaveEnabled = false

        // Aseguramos que el cursor sea siempre visible y solicitamos el foco.
        editResult.isCursorVisible = true
        editResult.requestFocus()

        // Deshabilita la aparición del teclado al obtener el foco.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editResult.setShowSoftInputOnFocus(false)
        } else {
            editResult.setInputType(android.text.InputType.TYPE_NULL)
        }

        // Restaura la expresión si existe.
        savedInstanceState?.getString("expression")?.let {
            expression = StringBuilder(it)
            editResult.setText(expression.toString())
            // Posiciona el cursor al final.
            editResult.setSelection(expression.length)
        }

        // Lista de botones que insertan caracteres en el EditText.
        val buttons = listOf(
            R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11,
            R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button18,
            R.id.button19, R.id.button20,
            R.id.button24, R.id.button25, R.id.button26, R.id.button27, R.id.button28,
            R.id.button29, R.id.button30, R.id.button31, R.id.button32, R.id.button33,
            R.id.button34, R.id.button35
        )

        buttons.forEach { id ->
            findViewById<Button>(id)?.setOnClickListener { view ->
                val btnText = (view as Button).text.toString()
                when {
                    // Para funciones que requieren paréntesis se arma el string completo.
                    btnText in listOf("sin", "cos", "tan", "asin", "acos", "atan",
                        "log", "In", "√", "10^x", "e^x") -> {
                        val functionText = when (btnText) {
                            "In" -> "ln("
                            "log" -> "log10("   // Al presionar "log", se inserta "log10(".
                            "√" -> "sqrt("
                            "10^x" -> "10^("
                            "e^x" -> "e^("
                            else -> "$btnText("
                        }
                        insertAtCursor(functionText)
                    }
                    btnText == "^2" -> {
                        insertAtCursor("^2")
                    }
                    else -> {
                        insertAtCursor(btnText)
                    }
                }
            }
        }

        // Botón AC: limpia la expresión.
        findViewById<Button>(R.id.button)?.setOnClickListener {
            expression.clear()
            editResult.setText("")
        }

        // Botón ⌫: elimina el carácter anterior a la posición del cursor.
        findViewById<Button>(R.id.button17)?.setOnClickListener {
            deleteAtCursor()
        }

        // Botón "=": evalúa la expresión completa.
        findViewById<Button>(R.id.btnResult)?.setOnClickListener {
            val balancedExpression = balanceParentheses(expression.toString())
            val resultado = calcularExpresion(balancedExpression)

            editResult.setText(resultado)
            expression.clear()
            expression.append(resultado)
            // Posiciona el cursor al final tras calcular el resultado.
            editResult.setSelection(editResult.text.length)
        }

        // Botón "inv": alterna funciones normales e inversas.
        findViewById<Button>(R.id.btnInv)?.setOnClickListener {
            toggleInverseFunctions()
        }
    }

    /**
     * Inserta el texto en la posición actual del cursor.
     */
    private fun insertAtCursor(text: String) {
        // Si el EditText muestra "Error", reiniciamos la expresión.
        if (editResult.text.toString() == "Error") {
            expression.clear()
            editResult.setText("")
        }

        var newText = text

        // Si se trata de una función trigonométrica, verificamos si ya existe en la expresión.
        // Las funciones a controlar son: sin, cos, tan y sus inversas.
        val trigFunctions = listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan(")
        if (newText in trigFunctions) {
            if (expression.toString().contains(newText)) {
                Toast.makeText(
                    this,
                    "No se permiten funciones trigonométricas repetidas",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Verifica si se intenta insertar una función log repetida.
        if (newText == "log10(") {
            if (expression.toString().contains("log10(")) {
                Toast.makeText(
                    this,
                    "No se permiten funciones log repetidas",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Si la expresión está vacía, se aplican reglas especiales:
        // - Si se toca "00" como primer botón, no se inserta nada.
        // - Si se toca "0" o "." como primer botón, se inserta "0.".
        if (expression.isEmpty()) {
            when (newText) {
                "00" -> return  // No se permite "00" como primer botón.
                "0", "." -> newText = "0."
            }
        }

        // Obtenemos la posición actual del cursor en el EditText.
        var cursorPosition = editResult.selectionStart
        if (cursorPosition < 0) {
            cursorPosition = expression.length
        }

        // Evita insertar dos puntos seguidos.
        if (newText == ".") {
            if (cursorPosition > 0 && expression[cursorPosition - 1] == '.') {
                Toast.makeText(this, "No se permiten puntos seguidos", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Evita insertar dos operadores seguidos.
        if (newText.length == 1 && isOperator(newText[0]) && cursorPosition > 0) {
            if (isOperator(expression[cursorPosition - 1])) {
                return
            }
        }

        // Inserta el nuevo texto en la posición del cursor.
        expression.insert(cursorPosition, newText)
        // Actualiza el contenido del EditText y coloca el cursor justo después del texto insertado.
        editResult.setText(expression.toString())
        editResult.setSelection(cursorPosition + newText.length)
    }

    /**
     * Verifica si el carácter es un operador.
     */
    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^'
    }

    /**
     * Elimina el carácter anterior a la posición del cursor.
     */
    private fun deleteAtCursor() {
        var cursorPosition = editResult.selectionStart
        if (cursorPosition < 0) {
            cursorPosition = expression.length
        }
        if (cursorPosition > 0) {
            expression.deleteCharAt(cursorPosition - 1)
            editResult.setText(expression.toString())
            editResult.setSelection(cursorPosition - 1)
        }
    }

    /**
     * Alterna entre funciones normales e inversas.
     */
    private fun toggleInverseFunctions() {
        val btnInv = findViewById<Button>(R.id.btnInv)
        val sinButton = findViewById<Button>(R.id.button24)
        val cosButton = findViewById<Button>(R.id.button25)
        val tanButton = findViewById<Button>(R.id.button26)
        val logButton = findViewById<Button>(R.id.button30)
        val sqrtButton = findViewById<Button>(R.id.button35)
        val lnButton = findViewById<Button>(R.id.button31)

        isInverseMode = !isInverseMode

        btnInv.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isInverseMode) android.graphics.Color.parseColor("#BD2A2E")
            else android.graphics.Color.parseColor("#486966")
        )

        if (isInverseMode) {
            sinButton?.text = "asin"
            cosButton?.text = "acos"
            tanButton?.text = "atan"
            logButton?.text = "10^"
            sqrtButton?.text = "^2"
            lnButton?.text = "e^"
        } else {
            sinButton?.text = "sin"
            cosButton?.text = "cos"
            tanButton?.text = "tan"
            logButton?.text = "log"
            sqrtButton?.text = "√"
            lnButton?.text = "In"
        }
    }

    /**
     * Evalúa la expresión matemática y retorna el resultado.
     */
    private fun calcularExpresion(expresionStr: String): String {
        return try {
            val expr = Expression(expresionStr)
            val result = expr.calculate()
            if (expr.errorMessage.contains("Division by zero", ignoreCase = true)) {
                "No se puede dividir por 0"
            } else if (result.isNaN() || result.isInfinite()) {
                "Error"
            } else {
                formatResult(result)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    /**
     * Equilibra los paréntesis de la expresión antes de evaluarla.
     */
    private fun balanceParentheses(expresionStr: String): String {
        val openCount = expresionStr.count { it == '(' }
        val closeCount = expresionStr.count { it == ')' }
        return expresionStr + ")".repeat(openCount - closeCount)
    }

    /**
     * Formatea el resultado para eliminar ceros innecesarios.
     */
    private fun formatResult(result: Double): String {
        return String.format(Locale.US, "%.6f", result)
            .trimEnd('0')
            .trimEnd('.')
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("expression", expression.toString())
    }
}
