package com.jack.neuroapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

// ==================== DATA CLASSES ====================
data class Usuario(val nome: String, val nascimento: String, val email: String, val senha: String, val motivo: String)
data class Crise(val dataHora: String, val tipo: String, val subTipo: String)
data class Diario(val dataHora: String, val humor: String, val anotacao: String)
data class Medicamento(val nome: String, val mg: String, val compDia: Int, val compCaixa: Int, val dataCompra: String)
data class Consulta(val profissional: String, val especialidade: String, val data: String, val hora: String, val endereco: String)
data class Sono(val horaDeitar: String, val horaLevantar: String, val acordouDuranteNoite: Boolean, val pesadelos: Boolean, val medicamento: String?)

// ==================== MAIN ACTIVITY ====================
class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private val usuarios = mutableStateListOf<Usuario>()
    private val crises = mutableStateListOf<Crise>()
    private val diarios = mutableStateListOf<Diario>()
    private val medicamentos = mutableStateListOf<Medicamento>()
    private val consultas = mutableStateListOf<Consulta>()
    private val registrosSono = mutableStateListOf<Sono>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("NeuroAppPrefs", Context.MODE_PRIVATE)

        // Carregar dados salvos
        usuarios.addAll(loadList("usuarios"))
        crises.addAll(loadList("crises"))
        diarios.addAll(loadList("diarios"))
        medicamentos.addAll(loadList("medicamentos"))
        consultas.addAll(loadList("consultas"))
        registrosSono.addAll(loadList("sono"))

        setContent {
            AppNavigation()
        }
    }

    // ==================== PERSISTÊNCIA ====================
    private inline fun <reified T> loadList(key: String): List<T> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun <T> saveList(key: String, list: List<T>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    // ==================== NAVIGATION ====================
    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                LoginScreen(
                    onLogin = { email, senha ->
                        val usuario = usuarios.find { it.email == email && it.senha == senha }
                        if (usuario != null) navController.navigate("menu")
                    },
                    onCadastro = { navController.navigate("cadastro") }
                )
            }

            composable("cadastro") {
                CadastroScreen(
                    onSalvar = {
                        usuarios.add(it)
                        saveList("usuarios", usuarios)
                        navController.popBackStack()
                    },
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable("menu") {
                MenuScreen(
                    onCrises = { navController.navigate("crises") },
                    onDiario = { navController.navigate("diario") },
                    onMedicamentos = { navController.navigate("medicamentos") },
                    onConsultas = { navController.navigate("consultas") },
                    onSono = { navController.navigate("sono") }
                )
            }

            composable("crises") {
                CrisesScreen(onVoltar = {
                    saveList("crises", crises)
                    navController.popBackStack()
                })
            }

            composable("diario") {
                DiarioScreen(onVoltar = {
                    saveList("diarios", diarios)
                    navController.popBackStack()
                })
            }

            composable("medicamentos") {
                MedicamentosScreen(onVoltar = {
                    saveList("medicamentos", medicamentos)
                    navController.popBackStack()
                })
            }

            composable("consultas") {
                ConsultasScreen(onVoltar = {
                    saveList("consultas", consultas)
                    navController.popBackStack()
                })
            }

            composable("sono") {
                SonoScreen(onVoltar = {
                    saveList("sono", registrosSono)
                    navController.popBackStack()
                })
            }
        }
    }

    // ==================== TELAS ====================

    @Composable
    fun LoginScreen(onLogin: (String,String)->Unit, onCadastro: ()->Unit) {
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("NeuroApp", fontSize = 28.sp)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value=email,onValueChange={email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value=senha,onValueChange={senha=it},label={Text("Senha")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick={onLogin(email,senha)},modifier=Modifier.fillMaxWidth()){Text("Login")}
            Spacer(Modifier.height(12.dp))
            Button(onClick=onCadastro,modifier=Modifier.fillMaxWidth()){Text("Cadastro")}
        }
    }

    @Composable
    fun CadastroScreen(onSalvar:(Usuario)->Unit, onVoltar:()->Unit) {
        var nome by remember { mutableStateOf("") }
        var nascimento by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var motivo by remember { mutableStateOf("") }

        LazyColumn(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            item { Text("Cadastro", fontSize=24.sp) }
            item { OutlinedTextField(value=nome,onValueChange={nome=it},label={Text("Nome Completo")},modifier=Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value=nascimento,onValueChange={nascimento=it},label={Text("Data de Nascimento")},modifier=Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value=email,onValueChange={email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value=senha,onValueChange={senha=it},label={Text("Senha")},visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth()) }
            item {
                Text("Motivo do uso")
                listOf("Epilepsia","Psicologico","Ambos").forEach { m ->
                    Row(verticalAlignment=Alignment.CenterVertically){
                        RadioButton(selected=motivo==m,onClick={motivo=m})
                        Text(when(m){"Epilepsia"->"Epilepsia";"Psicologico"->"Cuidado Psicológico";else->"Ambos"})
                    }
                }
            }
            item { Button(onClick={if(nome.isNotBlank()&&nascimento.isNotBlank()&&email.isNotBlank()&&senha.isNotBlank()&&motivo.isNotBlank()) onSalvar(Usuario(nome,nascimento,email,senha,motivo))},modifier=Modifier.fillMaxWidth()){Text("Salvar")} }
            item { Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar")} }
        }
    }

    @Composable
    fun MenuScreen(onCrises:()->Unit, onDiario:()->Unit, onMedicamentos:()->Unit, onConsultas:()->Unit, onSono:()->Unit){
        Column(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)){
            Text("Menu", fontSize=24.sp)
            Button(onClick=onCrises,modifier=Modifier.fillMaxWidth()){Text("Crises")}
            Button(onClick=onDiario,modifier=Modifier.fillMaxWidth()){Text("Diário")}
            Button(onClick=onMedicamentos,modifier=Modifier.fillMaxWidth()){Text("Medicamentos")}
            Button(onClick=onConsultas,modifier=Modifier.fillMaxWidth()){Text("Consultas")}
            Button(onClick=onSono,modifier=Modifier.fillMaxWidth()){Text("Sono")}
        }
    }

    // ==================== CRISES ====================
    @Composable
    fun CrisesScreen(onVoltar:()->Unit){
        var tipo by remember { mutableStateOf("") }
        var subTipo by remember { mutableStateOf("") }
        val opcoes = listOf("Tônico-Clônicas","Tônicas","Clônicas","Mioclônicas","Atônicas","Espasmos Epilépticos","Ausência Típica","Ausência Atípica","Epilepsia Mioclônica","Convulsiva Não Observada","Déjà Vu","Jamais Vu","Estado Epiléptico")

        LazyColumn(modifier=Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{ Text("Registrar Crise", fontSize=22.sp)}
            item{ DropdownSimples("Tipo",opcoes,tipo){tipo=it} }
            item{ DropdownSimples("Subtipo",opcoes,subTipo){subTipo=it} }
            item{ Button(onClick={ if(tipo.isNotBlank() && subTipo.isNotBlank()){ crises.add(Crise(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),tipo,subTipo)) } },modifier=Modifier.fillMaxWidth()){Text("Salvar")}}
            item{ Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar ao Menu")}}
            items(crises){ Text("${it.dataHora} - ${it.tipo} / ${it.subTipo}") }
        }
    }

    @Composable
    fun DropdownSimples(label:String, opcoes:List<String>, selecionado:String, onSelecionar:(String)->Unit){
        var expandido by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth()){
            Text(label)
            Button(onClick={expandido=true}, modifier=Modifier.fillMaxWidth()){ Text(if(selecionado.isEmpty())"Selecionar" else selecionado) }
            DropdownMenu(expanded=expandido, onDismissRequest={expandido=false}){
                opcoes.forEach { DropdownMenuItem(onClick={ onSelecionar(it); expandido=false }){ Text(it) } }
            }
        }
    }

    // ==================== DIÁRIO ====================
    @Composable
    fun DiarioScreen(onVoltar:()->Unit){
        var anotacao by remember { mutableStateOf("") }
        var humor by remember { mutableStateOf("") }
        LazyColumn(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{ Text("Diário", fontSize=24.sp)}
            item{ OutlinedTextField(value=anotacao,onValueChange={anotacao=it},label={Text("Escreva seus pensamentos")},modifier=Modifier.fillMaxWidth().height(150.dp)) }
            item{ Text("Escolha seu humor") }
            item{ Row{ listOf("Bom","Neutro","Ruim").forEach{ t-> Row(Modifier.padding(end=12.dp),verticalAlignment=Alignment.CenterVertically){ RadioButton(selected=humor==t,onClick={humor=t}); Text(t) } } } }
            item{ Button(onClick={ if(humor.isNotEmpty() && anotacao.isNotBlank()) { diarios.add(Diario(SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(Date()),humor,anotacao)); anotacao=""; humor="" } },modifier=Modifier.fillMaxWidth()){Text("Salvar")} }
            item{ Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar ao Menu")} }
            items(diarios){ Text("${it.dataHora} - ${it.humor}: ${it.anotacao}") }
        }
    }

    // ==================== MEDICAMENTOS ====================
    @Composable
    fun MedicamentosScreen(onVoltar:()->Unit){
        var nome by remember { mutableStateOf("") }
        var mg by remember { mutableStateOf("") }
        var compDia by remember { mutableStateOf("") }
        var compCaixa by remember { mutableStateOf("") }
        var dataCompra by remember { mutableStateOf("") }

        LazyColumn(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{ Text("Medicamentos", fontSize=24.sp) }
            item{ OutlinedTextField(value=nome,onValueChange={nome=it},label={Text("Nome")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=mg,onValueChange={mg=it},label={Text("MG")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=compDia,onValueChange={compDia=it},label={Text("Comprimidos por dia")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=compCaixa,onValueChange={compCaixa=it},label={Text("Comprimidos por caixa")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=dataCompra,onValueChange={dataCompra=it},label={Text("Data da última compra")},modifier=Modifier.fillMaxWidth()) }
            item{ Button(onClick={ val dia=compDia.toIntOrNull()?:0; val caixa=compCaixa.toIntOrNull()?:0; if(nome.isNotBlank() && mg.isNotBlank() && dia>0 && caixa>0 && dataCompra.isNotBlank()){ medicamentos.add(Medicamento(nome,mg,dia,caixa,dataCompra)); nome=""; mg=""; compDia=""; compCaixa=""; dataCompra="" } },modifier=Modifier.fillMaxWidth()){Text("Salvar")} }
            item{ Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar ao Menu")} }
            items(medicamentos){ val sdf=SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()); val data= runCatching{ sdf.parse(it.dataCompra) }.getOrNull() ?: Date(); val diasTotal= if(it.compDia>0) (it.compCaixa.toDouble()/it.compDia.toDouble()).toInt() else 0; val fim=Calendar.getInstance().apply{ time=data; add(Calendar.DAY_OF_YEAR,diasTotal) }; Text("${it.nome} ${it.mg}mg - Acaba em: ${sdf.format(fim.time)}") }
        }
    }

    // ==================== CONSULTAS ====================
    @Composable
    fun ConsultasScreen(onVoltar:()->Unit){
        var prof by remember { mutableStateOf("") }
        var esp by remember { mutableStateOf("") }
        var data by remember { mutableStateOf("") }
        var hora by remember { mutableStateOf("") }
        var endereco by remember { mutableStateOf("") }

        LazyColumn(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{ Text("Consultas", fontSize=24.sp) }
            item{ OutlinedTextField(value=prof,onValueChange={prof=it},label={Text("Profissional")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=esp,onValueChange={esp=it},label={Text("Especialidade")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=data,onValueChange={data=it},label={Text("Data")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=hora,onValueChange={hora=it},label={Text("Hora")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=endereco,onValueChange={endereco=it},label={Text("Endereço")},modifier=Modifier.fillMaxWidth()) }
            item{ Button(onClick={ if(prof.isNotBlank() && esp.isNotBlank() && data.isNotBlank() && hora.isNotBlank() && endereco.isNotBlank()){ consultas.add(Consulta(prof,esp,data,hora,endereco)); prof=""; esp=""; data=""; hora=""; endereco="" } },modifier=Modifier.fillMaxWidth()){Text("Salvar")} }
            item{ Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar ao Menu")} }
            items(consultas){ Text("${it.data} ${it.hora} - ${it.profissional} (${it.especialidade})") }
        }
    }

    // ==================== SONO ====================
    @Composable
    fun SonoScreen(onVoltar:()->Unit){
        var deitar by remember { mutableStateOf("") }
        var levantar by remember { mutableStateOf("") }
        var acordou by remember { mutableStateOf(false) }
        var pesadelos by remember { mutableStateOf(false) }
        var med by remember { mutableStateOf("") }

        LazyColumn(modifier=Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(8.dp)){
            item{ Text("Sono", fontSize=24.sp)}
            item{ OutlinedTextField(value=deitar,onValueChange={deitar=it},label={Text("Hora deitar")},modifier=Modifier.fillMaxWidth()) }
            item{ OutlinedTextField(value=levantar,onValueChange={levantar=it},label={Text("Hora levantar")},modifier=Modifier.fillMaxWidth()) }
            item{ Row(verticalAlignment=Alignment.CenterVertically){ Checkbox(checked=acordou,onCheckedChange={acordou=it}); Text("Acordou durante a noite?") } }
            item{ Row(verticalAlignment=Alignment.CenterVertically){ Checkbox(checked=pesadelos,onCheckedChange={pesadelos=it}); Text("Teve pesadelos?") } }
            item{ OutlinedTextField(value=med,onValueChange={med=it},label={Text("Medicamento se tomou")},modifier=Modifier.fillMaxWidth()) }
            item{ Button(onClick={ registrosSono.add(Sono(deitar,levantar,acordou,pesadelos,if(med.isBlank())null else med)); deitar=""; levantar=""; acordou=false; pesadelos=false; med="" },modifier=Modifier.fillMaxWidth()){Text("Salvar") } }
            item{ Button(onClick=onVoltar,modifier=Modifier.fillMaxWidth()){Text("Voltar ao Menu") } }
            items(registrosSono){ Text("${it.horaDeitar}-${it.horaLevantar}, Acordou: ${it.acordouDuranteNoite}, Pesadelos: ${it.pesadelos}, Med: ${it.medicamento ?: "-"}") }
        }
    }

}
