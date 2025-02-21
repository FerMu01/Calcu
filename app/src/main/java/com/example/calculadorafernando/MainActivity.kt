package com.example.calculadorafernando

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.Expression
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var textResult: TextView
    private var expression = StringBuilder()
    private var isInverseMode = false  // Variable para rastrear el estado de las funciones inversas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = android.graphics.Color.parseColor("#3B3936")

        textResult = findViewById(R.id.textResult)
        textResult.isSaveEnabled = false

        savedInstanceState?.getString("expression")?.let {
            expression = StringBuilder(it)
            textResult.text = expression.toString()
        }

// ids
val buttons = listOf(
            R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11,
            R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button18,
            R.id.button19, R.id.button20, R.id.button21, R.id.button23,
            R.id.button24, R.id.button25, R.id.button26, R.id.button27, R.id.button28,
            R.id.button29, R.id.button30, R.id.button31, R.id.button32, R.id.button33,
            R.id.button34, R.id.button35
        )

        buttons.forEach { id ->
            findViewById<Button?>(id)?.setOnClickListener { view ->
                val btnText = (view as Button).text.toString()

                when {
                    btnText in listOf("sin", "cos", "tan", "asin", "acos", "atan",
                        "log", "In", "√", "10^x", "e^x") -> {
                        val functionText = when (btnText) {
                            "In" -> "ln("
                            "log" -> "log10("
                            "√" -> "sqrt("
                            "10^x" -> "10^("
                            "e^x" -> "e^("
                            else -> "$btnText("
                        }
                        expression.append(functionText)
                    }
                    btnText == "^2" -> {
                        expression.append("^2")
                    }
                    else -> {
                        expression.append(btnText)
                    }
                }
                textResult.text = expression.toString()
            }
        }

        findViewById<Button>(R.id.button)?.setOnClickListener {
            expression.clear()
            textResult.text = ""
        }

        findViewById<Button>(R.id.button17)?.setOnClickListener {
            if (expression.isNotEmpty()) {
                expression.deleteCharAt(expression.length - 1)
                textResult.text = expression.toString()
            }
        }

        findViewById<Button>(R.id.btnResult)?.setOnClickListener {
            var processedExpression = expression.toString()
            processedExpression = processedExpression.replace("\\^2".toRegex(), "^2")

            val balancedExpression = balanceParentheses(processedExpression)
            val resultado = calcularExpresion(balancedExpression)

            textResult.text = resultado
            expression.clear()
            expression.append(resultado)
        }

        findViewById<Button>(R.id.btnInv)?.setOnClickListener {
            toggleInverseFunctions()
        }
    }

    private fun toggleInverseFunctions() {
        val btnInv = findViewById<Button>(R.id.btnInv)
        val sinButton = findViewById<Button>(R.id.button24)
        val cosButton = findViewById<Button>(R.id.button25)
        val tanButton = findViewById<Button>(R.id.button26)
        val logButton = findViewById<Button>(R.id.button30)
        val sqrtButton = findViewById<Button>(R.id.button35)
        val lnButton = findViewById<Button>(R.id.button31)

        isInverseMode = !isInverseMode

        if (isInverseMode) {
            btnInv.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BD2A2E"))
        } else {
            btnInv.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#486966"))
        }

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
