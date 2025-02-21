package com.example.calculadorafernando

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

        // Deshabilita la aparición del teclado al obtener el foco.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editResult.setShowSoftInputOnFocus(false)
        } else {
            // Para versiones anteriores, se puede usar este método.
            editResult.setInputType(android.text.InputType.TYPE_NULL)
        }

        savedInstanceState?.getString("expression")?.let {
            expression = StringBuilder(it)
            editResult.setText(expression.toString())
        }

        // Lista de botones que insertan caracteres en el EditText
        val buttons = listOf(
            R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11,
            R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button18
        )

        buttons.forEach { id ->
            findViewById<Button?>(id)?.setOnClickListener { view ->
                val btnText = (view as Button).text.toString()
                insertAtCursor(btnText)
            }
        }

        // Botón AC: limpia la expresión
        findViewById<Button>(R.id.button)?.setOnClickListener {
            expression.clear()
            editResult.setText("")
        }

        // Botón ⌫: elimina el carácter anterior a la posición del cursor
        findViewById<Button>(R.id.button17)?.setOnClickListener {
            deleteAtCursor()
        }

        // Botón "=": evalúa la expresión
        findViewById<Button>(R.id.btnResult)?.setOnClickListener {
            val balancedExpression = balanceParentheses(expression.toString())
            val resultado = calcularExpresion(balancedExpression)

            editResult.setText(resultado)
            expression.clear()
            expression.append(resultado)
        }

        // Botón "inv": alterna funciones inversas
        findViewById<Button>(R.id.btnInv)?.setOnClickListener {
            toggleInverseFunctions()
        }
    }

    /**
     * Inserta el texto recibido en la posición actual del cursor.
     */
    private fun insertAtCursor(text: String) {
        val cursorPosition = editResult.selectionStart.takeIf { it >= 0 } ?: expression.length
        expression.insert(cursorPosition, text)
        editResult.setText(expression.toString())
        editResult.setSelection(cursorPosition + text.length)
    }

    /**
     * Elimina el carácter anterior a la posición del cursor.
     */
    private fun deleteAtCursor() {
        val cursorPosition = editResult.selectionStart
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
            logButton?.text = "10^x"
            sqrtButton?.text = "^2"
            lnButton?.text = "e^x"
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
            if (result.isNaN() || result.isInfinite()) "Error"
            else String.format(Locale.US, "%.6f", result)
                .trimEnd('0')
                .trimEnd('.')
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("expression", expression.toString())
    }
}
