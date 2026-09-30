package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuestionEntity
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class representing a competition question
data class LiveQuizItem(
    val id: String,
    val questionBengali: String,
    val questionEnglish: String,
    val optionsBengali: List<String>,
    val optionsEnglish: List<String>,
    val correctIndex: Int,
    val explanationBengali: String,
    val explanationEnglish: String
)

data class LiveSubjectArena(
    val id: String,
    val titleBengali: String,
    val titleEnglish: String,
    val subtitleBengali: String,
    val subtitleEnglish: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val onlineCount: Int,
    val questions: List<LiveQuizItem>
)

data class LiveLeaderboardUser(
    val name: String,
    val district: String = "",
    val score: Int,
    val avatarColor: Color,
    val isUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveQuizScreen(
    viewModel: DeshExamViewModel? = null,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val dbQuestions = viewModel?.questions?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val studentProfile = viewModel?.studentProfile?.collectAsStateWithLifecycle()?.value

    // Curated high-yield 10-question sets per subject for Madhyamik & Board exams
    val arenas = remember(dbQuestions) {
        listOf(
            LiveSubjectArena(
                id = "bengali",
                titleBengali = "বাংলা",
                titleEnglish = "Bengali Arena",
                subtitleBengali = "গল্প, কবিতা, নাটক ও ব্যাকরণ",
                subtitleEnglish = "Literature, Poetry & Grammar",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                primaryColor = Color(0xFF8B5CF6),
                onlineCount = 560,
                questions = listOf(
                    LiveQuizItem(
                        id = "ben_1",
                        questionBengali = "বন্ধুরা জগদীশবাবুর কাছে কেন যাবে বলেছিল?",
                        questionEnglish = "Why did the friends plan to visit Jagadish Babu's residence?",
                        optionsBengali = listOf("স্পোর্টের চাঁদা নিতে", "খেলা দেখতে", "বেড়াতে", "নিমন্ত্রণ খেতে"),
                        optionsEnglish = listOf("To collect sports contribution", "To watch games", "For outing", "For invitation"),
                        correctIndex = 0,
                        explanationBengali = "সঠিক উত্তর: স্পোর্টের চাঁদা নিতে। তারা স্পোর্টের জন্য অনুদান সংগ্রহে গিয়েছিল।",
                        explanationEnglish = "Correct answer: To collect sports subscription."
                    ),
                    LiveQuizItem(
                        id = "ben_2",
                        questionBengali = "'জ্ঞানচক্ষু' গল্পে তপনের প্রথম গল্পটির নাম কী ছিল?",
                        questionEnglish = "What was the title of Tapan's first story in 'Gyanchakshu'?",
                        optionsBengali = listOf("প্রথম দিন", "স্কুলের স্মৃতি", "ভোরবেলা", "ছুটির দিন"),
                        optionsEnglish = listOf("Prothom Din", "School Memories", "Early Dawn", "Vacation"),
                        correctIndex = 0,
                        explanationBengali = "তপন তার স্কুলে ভরতি হওয়ার রোমাঞ্চকর অভিজ্ঞতা নিয়ে 'প্রথম দিন' গল্পটি লিখেছিল।",
                        explanationEnglish = "Tapan authored 'Prothom Din' recounting his first day at school."
                    ),
                    LiveQuizItem(
                        id = "ben_3",
                        questionBengali = "'বহুরূপী' গল্পে হরিদা সপ্তাহে কতদিন বহুরূপী সাজতেন?",
                        questionEnglish = "In the story 'Bahurupi', how many days a week did Harida disguise himself?",
                        optionsBengali = listOf("একদিন", "দুইদিন", "তিনদিন", "প্রতিদিন"),
                        optionsEnglish = listOf("One day", "Two days", "Three days", "Every day"),
                        correctIndex = 0,
                        explanationBengali = "হরিদা সপ্তাহে মাত্র একদিন নানা বিচিত্র বেশে সেজে অর্থ উপার্জন করতেন।",
                        explanationEnglish = "Harida adopted colorful disguises only once a week."
                    ),
                    LiveQuizItem(
                        id = "ben_4",
                        questionBengali = "'অদল বদল' গল্পে অমৃত ও ইসাবের বন্ধুত্বের মূল বার্তা কী?",
                        questionEnglish = "What is the core message of friendship in 'Adal Badal'?",
                        optionsBengali = listOf("সম্প্রীতি ও নিঃস্বার্থ ভালোবাসা", "খেলাধুলো", "পোশাক পরিবর্তন", "লড়াই"),
                        optionsEnglish = listOf("Harmony and unconditional love", "Sports", "Changing clothes", "Fighting"),
                        correctIndex = 0,
                        explanationBengali = "হিন্দু-মুসলিম দুই বালকের অমূল্য ভালোবাসা সকল ভেদাভেদ অতিক্রম করে যায়।",
                        explanationEnglish = "The narrative champions deep brotherhood and communal harmony."
                    ),
                    LiveQuizItem(
                        id = "ben_5",
                        questionBengali = "'পথের দাবী' উপন্যাসে রাজবন্দি সব্যসাচী মল্লিকের পেশা কী ছিল?",
                        questionEnglish = "What was the profession of revolutionary Sabyasachi Mallik in 'Pather Dabi'?",
                        optionsBengali = listOf("ডাক্তার", "উকিল", "শিক্ষক", "ইঞ্জিনিয়ার"),
                        optionsEnglish = listOf("Doctor", "Lawyer", "Teacher", "Engineer"),
                        correctIndex = 0,
                        explanationBengali = "সব্যসাচী মল্লিক ছিলেন একজন অত্যন্ত দক্ষ বিলাতফেরত চিকিৎসক ও বিপ্লবী নেতা।",
                        explanationEnglish = "Sabyasachi Mallik was an overseas educated physician and patriot."
                    ),
                    LiveQuizItem(
                        id = "ben_6",
                        questionBengali = "'অসুখী একজন' কবিতাটির মূল কবি পাবলো নেরুদা কোন দেশের মানুষ?",
                        questionEnglish = "Pablo Neruda, the author of 'Asukhi Ekjan', hailed from which country?",
                        optionsBengali = listOf("চিলি (ল্যাটিন আমেরিকা)", "স্পেন", "মেক্সিকো", "আর্জেন্টিনা"),
                        optionsEnglish = listOf("Chile", "Spain", "Mexico", "Argentina"),
                        correctIndex = 0,
                        explanationBengali = "নোবেলজয়ী কবি পাবলো নেরুদা চিলির বিখ্যাত কবি ও কূটনীতিবিদ ছিলেন।",
                        explanationEnglish = "Nobel laureate Pablo Neruda was a renowned Chilean poet."
                    ),
                    LiveQuizItem(
                        id = "ben_7",
                        questionBengali = "বাংলা ব্যাকরণে যে বিভক্তি সর্বক্ষেত্রে প্রযুক্ত হতে পারে তাকে কী বলে?",
                        questionEnglish = "What is a grammatical case inflection called that applies ubiquitously?",
                        optionsBengali = listOf("তির্যক বিভক্তি ('এ' বিভক্তি)", "শূন্য বিভক্তি", "দ্বৈত বিভক্তি", "অপ্রধান বিভক্তি"),
                        optionsEnglish = listOf("Tiryak Inflection", "Zero Inflection", "Dual Inflection", "Minor"),
                        correctIndex = 0,
                        explanationBengali = "'এ' বিভক্তি প্রায় সব কারকেই ব্যবহৃত হতে পারে বলে একে তির্যক বিভক্তি বলা হয়।",
                        explanationEnglish = "The 'E' affix functions as a flexible Tiryak case marker in Bengali."
                    ),
                    LiveQuizItem(
                        id = "ben_8",
                        questionBengali = "'দশ আনন যার = দশানন' এটি কোন সমাসের উদাহরণ?",
                        questionEnglish = "What type of compound (Samas) is 'Dashanan'?",
                        optionsBengali = listOf("বহুব্রীহি সমাস", "তৎপুরুষ সমাস", "দ্বন্দ্ব সমাস", "দ্বিগু সমাস"),
                        optionsEnglish = listOf("Bahuvrihi Samas", "Tatpurush", "Dwandwa", "Dvigu"),
                        correctIndex = 0,
                        explanationBengali = "অন্য পদ (রাবণ)-কে প্রাধান্য দেওয়ায় এটি বহুব্রীহি সমাস।",
                        explanationEnglish = "It refers to a third entity (Ravana), exemplifying Bahuvrihi."
                    ),
                    LiveQuizItem(
                        id = "ben_9",
                        questionBengali = "বাংলা সাহিত্যে 'কল্লোল' যুগের অন্যতম প্রধান বৈশিষ্ট্য কী ছিল?",
                        questionEnglish = "What was a defining characteristic of the Kallol era in Bengali literature?",
                        optionsBengali = listOf("বাস্তববাদ ও আধুনিকতার উন্মেষ", "ধর্মীয় কাব্য", "রূপকথা", "সংস্কৃত অনুবাদ"),
                        optionsEnglish = listOf("Realism & Modernity", "Religious verses", "Folk tales", "Sanskrit translation"),
                        correctIndex = 0,
                        explanationBengali = "মধ্যবিত্ত ও শ্রমজীবী মানুষের দুঃখ-বেদনা সাহিত্যে স্থান পায়।",
                        explanationEnglish = "Kallol era introduced urban realism and bold modern expressions."
                    ),
                    LiveQuizItem(
                        id = "ben_10",
                        questionBengali = "'সিরাজউদ্দৌলা' নাট্যাংশে মীরজাফরের বিশ্বাসঘাতকতার রূপ ফুটে উঠেছে কোন যুদ্ধের পটভূমিকায়?",
                        questionEnglish = "Against which historical battle is Mir Jafar's treachery depicted in 'Siraj-ud-Daulah'?",
                        optionsBengali = listOf("পলাশীর যুদ্ধ (১৭৫৭)", "বক্সারের যুদ্ধ", "পানিপথের যুদ্ধ", "কর্ণাটকের যুদ্ধ"),
                        optionsEnglish = listOf("Battle of Plassey (1757)", "Battle of Buxar", "Panipat", "Carnatic"),
                        correctIndex = 0,
                        explanationBengali = "২৩শে জুন ১৭৫৭ সালের পলাশীর প্রান্তরে সিরাজের পরাজয় ও পরাধীনতার কাহিনী রয়েছে।",
                        explanationEnglish = "The narrative centers upon the tragic betrayal during the Battle of Plassey."
                    )
                )
            ),
            LiveSubjectArena(
                id = "english",
                titleBengali = "ইংরেজি",
                titleEnglish = "English Arena",
                subtitleBengali = "গ্রামার, ভোকাবুলারি ও কম্প্রিহেনশন",
                subtitleEnglish = "Grammar, Vocabulary & Comprehension",
                icon = Icons.Default.Translate,
                primaryColor = Color(0xFF0284C7),
                onlineCount = 470,
                questions = listOf(
                    LiveQuizItem(
                        id = "eng_1",
                        questionBengali = "Choose the correct preposition: \"He is senior _____ me in service.\"",
                        questionEnglish = "Choose the correct preposition: \"He is senior _____ me in service.\"",
                        optionsBengali = listOf("to", "than", "with", "of"),
                        optionsEnglish = listOf("to", "than", "with", "of"),
                        correctIndex = 0,
                        explanationBengali = "senior, junior, superior, inferior-এর পর preposition 'to' বসে, 'than' নয়।",
                        explanationEnglish = "Latin comparative adjectives take 'to'."
                    ),
                    LiveQuizItem(
                        id = "eng_2",
                        questionBengali = "Choose the correct phrasal verb for 'rejected': \"The committee rejected our proposal.\"",
                        questionEnglish = "Choose the correct phrasal verb for 'rejected': \"The committee rejected our proposal.\"",
                        optionsBengali = listOf("turned down", "gave away", "brought up", "called off"),
                        optionsEnglish = listOf("turned down", "gave away", "brought up", "called off"),
                        correctIndex = 0,
                        explanationBengali = "'turned down' মানে হলো বাতিল বা প্রত্যাখ্যান (rejected) করা।",
                        explanationEnglish = "'turned down' means rejected."
                    ),
                    LiveQuizItem(
                        id = "eng_3",
                        questionBengali = "Fill in the blank: \"Neither of the boys _____ present in the class yesterday.\"",
                        questionEnglish = "Fill in the blank: \"Neither of the boys _____ present in the class yesterday.\"",
                        optionsBengali = listOf("was", "were", "are", "have been"),
                        optionsEnglish = listOf("was", "were", "are", "have been"),
                        correctIndex = 0,
                        explanationBengali = "\"Neither of\"-এর পর plural noun থাকলেও verb সর্বদা singular (was) হয়।",
                        explanationEnglish = "Neither of takes a singular verb."
                    ),
                    LiveQuizItem(
                        id = "eng_4",
                        questionBengali = "Identify the correct passive voice: \"Someone stole my bicycle.\"",
                        questionEnglish = "Identify the correct passive voice: \"Someone stole my bicycle.\"",
                        optionsBengali = listOf("My bicycle was stolen", "My bicycle is stolen", "My bicycle was stole", "My bicycle had stolen"),
                        optionsEnglish = listOf("My bicycle was stolen", "My bicycle is stolen", "My bicycle was stole", "My bicycle had stolen"),
                        correctIndex = 0,
                        explanationBengali = "Past Indefinite passive voice: object + was/were + V3 (stolen)। অজ্ঞাত কর্তায় 'by someone' বাদ যায়।",
                        explanationEnglish = "The passive form is 'was stolen'."
                    ),
                    LiveQuizItem(
                        id = "eng_5",
                        questionBengali = "Choose the correct spelling:",
                        questionEnglish = "Choose the correct spelling:",
                        optionsBengali = listOf("Accommodation", "Acommodation", "Accomodation", "Acomation"),
                        optionsEnglish = listOf("Accommodation", "Acommodation", "Accomodation", "Acomation"),
                        correctIndex = 0,
                        explanationBengali = "সঠিক বানান হলো A-c-c-o-m-m-o-d-a-t-i-o-n (ডবল 'c' এবং ডবল 'm')।",
                        explanationEnglish = "Accommodation has double c and double m."
                    ),
                    LiveQuizItem(
                        id = "eng_6",
                        questionBengali = "Select the synonym of 'Enormous':",
                        questionEnglish = "Select the synonym of 'Enormous':",
                        optionsBengali = listOf("Huge", "Tiny", "Weak", "Fragile"),
                        optionsEnglish = listOf("Huge", "Tiny", "Weak", "Fragile"),
                        correctIndex = 0,
                        explanationBengali = "'Enormous' শব্দের অর্থ বিশালাকার বা প্রকাণ্ড, যার সমার্থক শব্দ 'Huge'।",
                        explanationEnglish = "Huge means enormous."
                    ),
                    LiveQuizItem(
                        id = "eng_7",
                        questionBengali = "What is the antonym (বিপরীত শব্দ) of 'Optimistic'?",
                        questionEnglish = "What is the antonym of 'Optimistic'?",
                        optionsBengali = listOf("Pessimistic", "Hopeful", "Cheerful", "Confident"),
                        optionsEnglish = listOf("Pessimistic", "Hopeful", "Cheerful", "Confident"),
                        correctIndex = 0,
                        explanationBengali = "'Optimistic' (আশাবাদী)-এর বিপরীত শব্দ হলো 'Pessimistic' (হতাশাবাদী)।",
                        explanationEnglish = "Pessimistic is the antonym of optimistic."
                    ),
                    LiveQuizItem(
                        id = "eng_8",
                        questionBengali = "Fill in the blank: \"If it rains, we _____ cancel the match.\"",
                        questionEnglish = "Fill in the blank: \"If it rains, we _____ cancel the match.\"",
                        optionsBengali = listOf("will", "would", "had", "would have"),
                        optionsEnglish = listOf("will", "would", "had", "would have"),
                        correctIndex = 0,
                        explanationBengali = "First conditional বাক্যে if clause Present Tense হলে main clause-এ will বসে।",
                        explanationEnglish = "First conditional uses will."
                    ),
                    LiveQuizItem(
                        id = "eng_9",
                        questionBengali = "One-word substitution: \"A person who loves and collects books\"",
                        questionEnglish = "One-word substitution: \"A person who loves and collects books\"",
                        optionsBengali = listOf("Bibliophile", "Philanthropist", "Polyglot", "Misogynist"),
                        optionsEnglish = listOf("Bibliophile", "Philanthropist", "Polyglot", "Misogynist"),
                        correctIndex = 0,
                        explanationBengali = "যিনি বই ভালোবাসেন এবং সংগ্রহ করেন তাকে এক কথায় 'Bibliophile' বলা হয়।",
                        explanationEnglish = "A bibliophile loves books."
                    ),
                    LiveQuizItem(
                        id = "eng_10",
                        questionBengali = "Indirect speech of: He said, \"I am reading an interesting novel.\"",
                        questionEnglish = "Indirect speech of: He said, \"I am reading an interesting novel.\"",
                        optionsBengali = listOf("He said that he was reading an interesting novel", "He said that he is reading an interesting novel", "He told he reads an interesting novel", "He said he read an interesting novel"),
                        optionsEnglish = listOf("He said that he was reading an interesting novel", "He said that he is reading an interesting novel", "He told he reads an interesting novel", "He said he read an interesting novel"),
                        correctIndex = 0,
                        explanationBengali = "Reporting verb past tense-এ থাকায় Present Continuous পরিবর্তিত হয়ে Past Continuous (was reading) হবে।",
                        explanationEnglish = "Present continuous becomes past continuous."
                    )
                )
            ),
            LiveSubjectArena(
                id = "history",
                titleBengali = "ইতিহাস",
                titleEnglish = "History Arena",
                subtitleBengali = "জাতীয় আন্দোলন, সংস্কার ও বিদ্রোহ",
                subtitleEnglish = "National Movement & Freedom Struggle",
                icon = Icons.Default.HistoryEdu,
                primaryColor = Color(0xFFD97706),
                onlineCount = 380,
                questions = listOf(
                    LiveQuizItem(
                        id = "hist_1",
                        questionBengali = "ভারতে প্রথম রেলপথের শুভ সূচনা কোন বছর হয়েছিল?",
                        questionEnglish = "In which year was the first railway line opened in India?",
                        optionsBengali = listOf("১৮৫৩ সালে", "১৮৫৭ সালে", "১৮৬২ সালে", "১৯০৫ সালে"),
                        optionsEnglish = listOf("1853", "1857", "1862", "1905"),
                        correctIndex = 0,
                        explanationBengali = "১৬ই এপ্রিল ১৮৫৩ সালে বোম্বে থেকে থানে পর্যন্ত প্রথম যাত্রীবাহী ট্রেন চালু হয়।",
                        explanationEnglish = "First passenger train ran between Bombay and Thane in 1853."
                    ),
                    LiveQuizItem(
                        id = "hist_2",
                        questionBengali = "বিখ্যাত 'নীলদর্পণ' নাটকের রচয়িতা কে ছিলেন?",
                        questionEnglish = "Who was the playwright of the famous drama 'Neel Darpan'?",
                        optionsBengali = listOf("দীনবন্ধু মিত্র", "মাইকেল মধুসূদন দত্ত", "গিরিশচন্দ্র ঘোষ", "রবীন্দ্রনাথ ঠাকুর"),
                        optionsEnglish = listOf("Dinabandhu Mitra", "Michael Madhusudan Dutta", "Girish Ghosh", "Rabindranath Tagore"),
                        correctIndex = 0,
                        explanationBengali = "নীলকর সাহেবদের অত্যাচারের বাস্তব কাহিনী নিয়ে দীনবন্ধু মিত্র নীলদর্পণ রচনা করেন।",
                        explanationEnglish = "Dinabandhu Mitra exposed indigo planter oppression in Neel Darpan."
                    ),
                    LiveQuizItem(
                        id = "hist_3",
                        questionBengali = "১৮৫৭ সালের মহাবিদ্রোহে প্রথম শহীদের মর্যাদা পান কে?",
                        questionEnglish = "Who is regarded as the first martyr of the Revolt of 1857?",
                        optionsBengali = listOf("মঙ্গল পাণ্ডে", "তাঁতিয়া তোপী", "নানাসাহেব", "বখত খান"),
                        optionsEnglish = listOf("Mangal Pandey", "Tatya Tope", "Nana Sahib", "Bakht Khan"),
                        correctIndex = 0,
                        explanationBengali = "ব্যারাকপুর সেনানিবাসে চর্বিযুক্ত কার্তুজ ব্যবহারে বিদ্রোহ ঘোষণা করে মঙ্গল পাণ্ডে শহীদ হন।",
                        explanationEnglish = "Mangal Pandey sparked the rebellion at Barrackpore."
                    ),
                    LiveQuizItem(
                        id = "hist_4",
                        questionBengali = "'বন্দে মাতরম্' সঙ্গীতের উৎস 'আনন্দমঠ' উপন্যাস কার লেখা?",
                        questionEnglish = "Who authored the patriotic novel 'Anandamath' containing 'Vande Mataram'?",
                        optionsBengali = listOf("বঙ্কিমচন্দ্র চট্টোপাধ্যায়", "শরৎচন্দ্র চট্টোপাধ্যায়", "ঈশ্বরচন্দ্র বিদ্যাসাগর", "দ্বিজেন্দ্রলাল রায়"),
                        optionsEnglish = listOf("Bankim Chandra Chattopadhyay", "Sarat Chandra", "Vidyasagar", "D.L. Roy"),
                        correctIndex = 0,
                        explanationBengali = "১৮৮২ সালে সন্ন্যাসী বিদ্রোহের পটভূমিকায় বঙ্কিমচন্দ্র চট্টোপাধ্যায় আনন্দমঠ রচনা করেন।",
                        explanationEnglish = "Bankim Chandra Chattopadhyay penned Anandamath in 1882."
                    ),
                    LiveQuizItem(
                        id = "hist_5",
                        questionBengali = "কলকাতায় 'ভারত সভা'  কত সালে প্রতিষ্ঠিত হয়?",
                        questionEnglish = "In which year was the Indian Association founded in Calcutta?",
                        optionsBengali = listOf("১৮৭৬ সালে", "১৮৮৫ সালে", "১৮৬৬ সালে", "১৯০৬ সালে"),
                        optionsEnglish = listOf("1876", "1885", "1866", "1906"),
                        correctIndex = 0,
                        explanationBengali = "সুরেন্দ্রনাথ বন্দ্যোপাধ্যায় ও আনন্দমোহন বসু ২৬শে জুলাই ১৮৭৬ সালে ভারত সভা স্থাপন করেন।",
                        explanationEnglish = "Surendranath Banerjee founded the Indian Association in 1876."
                    ),
                    LiveQuizItem(
                        id = "hist_6",
                        questionBengali = "কলকাতায় 'বসু বিজ্ঞান মন্দির'  কে প্রতিষ্ঠা করেন?",
                        questionEnglish = "Who founded the Bose Institute in Calcutta?",
                        optionsBengali = listOf("আচার্য জগদীশচন্দ্র বসু", "প্রফুল্লচন্দ্র রায়", "সত্যেন্দ্রনাথ বসু", "মেঘনাদ সাহা"),
                        optionsEnglish = listOf("Jagadish Chandra Bose", "P.C. Ray", "S.N. Bose", "Meghnad Saha"),
                        correctIndex = 0,
                        explanationBengali = "১৯১৭ সালে ভারতে উচ্চতর বৈজ্ঞানিক গবেষণার উদ্দেশ্যে তিনি এটি প্রতিষ্ঠা করেন।",
                        explanationEnglish = "Sir J.C. Bose founded Bose Institute in 1917."
                    ),
                    LiveQuizItem(
                        id = "hist_7",
                        questionBengali = "লর্ড কার্জন কোন বছর বঙ্গভঙ্গ কার্যকর করেছিলেন?",
                        questionEnglish = "In which year was the Partition of Bengal implemented by Lord Curzon?",
                        optionsBengali = listOf("১৯০৫ সালে", "১৯১১ সালে", "১৯২০ সালে", "১৯৪৭ সালে"),
                        optionsEnglish = listOf("1905", "1911", "1920", "1947"),
                        correctIndex = 0,
                        explanationBengali = "১৬ই অক্টোবর ১৯০৫ সালে বঙ্গভঙ্গ কার্যকর হলে রবীন্দ্রনাথের আহ্বানে রাখীবন্ধন উৎসব পালিত হয়।",
                        explanationEnglish = "The partition of Bengal took effect on October 16, 1905."
                    ),
                    LiveQuizItem(
                        id = "hist_8",
                        questionBengali = "আজাদ হিন্দ ফৌজ  সফলভাবে পুনর্গঠন করেছিলেন কে?",
                        questionEnglish = "Who rejuvenated and led the Indian National Army (Azad Hind Fauj)?",
                        optionsBengali = listOf("নেতাজি সুভাষচন্দ্র বসু", "রাসবিহারী বসু", "মোহন সিং", "ভগৎ সিং"),
                        optionsEnglish = listOf("Netaji Subhash Chandra Bose", "Rashbehari Bose", "Mohan Singh", "Bhagat Singh"),
                        correctIndex = 0,
                        explanationBengali = "সিঙ্গাপুরে নেতাজি 'দিল্লি চলো' ধ্বনি দিয়ে আজাদ হিন্দ ফৌজের সর্বাধিনায়কত্ব গ্রহণ করেন।",
                        explanationEnglish = "Netaji Subhash Chandra Bose led the Azad Hind Fauj to fight for freedom."
                    ),
                    LiveQuizItem(
                        id = "hist_9",
                        questionBengali = "গান্ধীজির নেতৃত্বাধীন ডান্ডি অভিযান কোন আন্দোলনের সূচনা করেছিল?",
                        questionEnglish = "The Dandi March led by Mahatma Gandhi marked the beginning of which movement?",
                        optionsBengali = listOf("আইন অমান্য আন্দোলন", "অসহযোগ আন্দোলন", "ভারত ছাড়ো আন্দোলন", "খেলাফত আন্দোলন"),
                        optionsEnglish = listOf("Civil Disobedience Movement", "Non-Cooperation", "Quit India", "Khilafat"),
                        correctIndex = 0,
                        explanationBengali = "১৯৩০ সালের ৬ই এপ্রিল সমুদ্রের নোনা জল ফুটিয়ে লবণ তৈরির মাধ্যমে আইন অমান্য আন্দোলনের সূচনা হয়।",
                        explanationEnglish = "The Salt March triggered the Civil Disobedience Movement in 1930."
                    ),
                    LiveQuizItem(
                        id = "hist_10",
                        questionBengali = "ভারত স্বাধীন হওয়ার সময় ভারতের গভর্নর জেনারেল কে ছিলেন?",
                        questionEnglish = "Who was the Governor-General of India at the time of independence in 1947?",
                        optionsBengali = listOf("লর্ড মাউন্টব্যাটেন", "লর্ড ওয়াভেল", "সি. রাজাগোপালাচারী", "লর্ড লিনলিথগো"),
                        optionsEnglish = listOf("Lord Mountbatten", "Lord Wavell", "C. Rajagopalachari", "Lord Linlithgow"),
                        correctIndex = 0,
                        explanationBengali = "লর্ড মাউন্টব্যাটেন ১৫ই আগস্ট ১৯৪৭ সালে স্বাধীন ভারতের প্রথম গভর্নর জেনারেল হন।",
                        explanationEnglish = "Lord Mountbatten served as the last Viceroy and first post-independence Governor-General."
                    )
                )
            ),
            LiveSubjectArena(
                id = "geography",
                titleBengali = "ভূগোল",
                titleEnglish = "Geography Arena",
                subtitleBengali = "ভারতের ভূপ্রকৃতি, নদনদী ও জলবায়ু",
                subtitleEnglish = "Landforms, Rivers & Indian Climate",
                icon = Icons.Default.Public,
                primaryColor = Color(0xFF0D9488),
                onlineCount = 340,
                questions = listOf(
                    LiveQuizItem(
                        id = "geo_1",
                        questionBengali = "নদীর মোহনায় পাখির পায়ের মতো বদ্বীপ কোথায় দেখা যায়?",
                        questionEnglish = "Where is a bird's-foot delta formed?",
                        optionsBengali = listOf("মিসিসিপি-মিসৌরি নদী", "গঙ্গা-ব্রহ্মপুত্র", "নীলনদ", "আমাজন নদী"),
                        optionsEnglish = listOf("Mississippi-Missouri", "Ganga-Brahmaputra", "Nile", "Amazon"),
                        correctIndex = 0,
                        explanationBengali = "উত্তর আমেরিকার মিসিসিপি নদীর মোহনায় পাখির পায়ের আঙুলের মতো বদ্বীপ গড়ে উঠেছে।",
                        explanationEnglish = "The Mississippi River delta in North America is a classic bird's-foot delta."
                    ),
                    LiveQuizItem(
                        id = "geo_2",
                        questionBengali = "হিমবাহের ক্ষয়কার্যের ফলে সৃষ্ট ঝুলন্ত উপত্যকায় সাধারণত কী সৃষ্টি হয়?",
                        questionEnglish = "What geological feature typically forms in a hanging valley created by glacial erosion?",
                        optionsBengali = listOf("জলপ্রপাত", "বদ্বীপ", "অশ্বক্ষুরাকৃতি হ্রদ", "বালিয়াড়ি"),
                        optionsEnglish = listOf("Waterfall", "Delta", "Oxbow Lake", "Sand Dune"),
                        correctIndex = 0,
                        explanationBengali = "উপহিমবাহের উপত্যকা মূল উপত্যকা থেকে উঁচুতে থাকায় জল নিচে পড়ে জলপ্রপাত সৃষ্টি করে।",
                        explanationEnglish = "Steep elevation drop in hanging valleys leads to waterfall formation."
                    ),
                    LiveQuizItem(
                        id = "geo_3",
                        questionBengali = "বায়ুমণ্ডলের সর্বনিম্ন ও শীতলতম স্তরের নাম কী?",
                        questionEnglish = "Which is the coldest layer in the Earth's atmosphere?",
                        optionsBengali = listOf("মেসোস্ফিয়ার ", "স্ট্র্যাটোস্ফিয়ার", "ট্রপোস্ফিয়ার", "থার্মোস্ফিয়ার"),
                        optionsEnglish = listOf("Mesosphere", "Stratosphere", "Troposphere", "Thermosphere"),
                        correctIndex = 0,
                        explanationBengali = "মেসোস্ফিয়ারের মেসোপজে তাপমাত্রা প্রায় -৯৩ ডিগ্রি সেলসিয়াসে নেমে যায়।",
                        explanationEnglish = "The mesosphere reaches minimum temperatures of around -93°C."
                    ),
                    LiveQuizItem(
                        id = "geo_4",
                        questionBengali = "দক্ষিণ ভারতের সর্বোচ্চ পর্বতশৃঙ্গ কোনটি?",
                        questionEnglish = "Which is the highest peak in South India?",
                        optionsBengali = listOf("আনাইমুদি ", "দোদাবেতা", "মহেন্দ্রগিরি", "গুরুশিখর"),
                        optionsEnglish = listOf("Anaimudi", "Doddabetta", "Mahendragiri", "Guru Shikhar"),
                        correctIndex = 0,
                        explanationBengali = "আন্নামালাই পর্বতের আনাইমুদি শৃঙ্গের উচ্চতা ২৬৯৫ মিটার যা দক্ষিণ ভারতের সর্বোচ্চ।",
                        explanationEnglish = "Anaimudi (2,695m) in the Western Ghats is the highest peak in South India."
                    ),
                    LiveQuizItem(
                        id = "geo_5",
                        questionBengali = "ভারতের কেন্দ্রীয় ধান গবেষণাগারটি কোথায় অবস্থিত?",
                        questionEnglish = "Where is the Central Rice Research Institute of India located?",
                        optionsBengali = listOf("কটক (ওড়িশা)", "দেরাদুন", "পুসা (দিল্লি)", "ব্যারাকপুর"),
                        optionsEnglish = listOf("Cuttack (Odisha)", "Dehradun", "Pusa", "Barrackpore"),
                        correctIndex = 0,
                        explanationBengali = "ওড়িশার কটকে জাতীয় ধান গবেষণা কেন্দ্র (NRRI) অবস্থিত।",
                        explanationEnglish = "National Rice Research Institute is situated in Cuttack, Odisha."
                    ),
                    LiveQuizItem(
                        id = "geo_6",
                        questionBengali = "ভারতের তথ্যপ্রযুক্তি শিল্পের রাজধানী  কাকে বলা হয়?",
                        questionEnglish = "Which city is known as the Silicon Valley / IT Capital of India?",
                        optionsBengali = listOf("বেঙ্গালুরু", "হায়দরাবাদ", "কলকাতা", "মুম্বাই"),
                        optionsEnglish = listOf("Bengaluru", "Hyderabad", "Kolkata", "Mumbai"),
                        correctIndex = 0,
                        explanationBengali = "কর্ণাটকের বেঙ্গালুরু ভারতের বৃহত্তম সফটওয়্যার ও প্রযুক্তি রপ্তানিকারক কেন্দ্র।",
                        explanationEnglish = "Bengaluru is recognized globally as the Silicon Valley of India."
                    ),
                    LiveQuizItem(
                        id = "geo_7",
                        questionBengali = "মহাসাগরের কোন জলবায়ু অঞ্চলে 'মরা কোটাল' সংঘটিত হয়?",
                        questionEnglish = "During which lunar phase does a Neap Tide (Mora Kotal) occur?",
                        optionsBengali = listOf("অষ্টমী তিথিতে", "অমাবস্যায়", "পূর্ণিমায়", "একাদশীতে"),
                        optionsEnglish = listOf("Ashtami (Quarter Moon)", "Amavasya", "Purnima", "Ekadashi"),
                        correctIndex = 0,
                        explanationBengali = "সূর্য ও চাঁদ যখন পৃথিবীর সাপেক্ষে ৯০° কোণে সমকোণে থাকে তখন মরা কোটাল হয়।",
                        explanationEnglish = "Neap tides occur during the first and third quarter lunar phases."
                    ),
                    LiveQuizItem(
                        id = "geo_8",
                        questionBengali = "ভারতের প্রাচীনতম ভঙ্গিল পর্বতমালা কোনটি?",
                        questionEnglish = "Which is the oldest fold mountain range in India?",
                        optionsBengali = listOf("আরাবল্লী পর্বত", "হিমালয়", "বিন্ধ্য পর্বত", "সাতপুরা"),
                        optionsEnglish = listOf("Aravalli Range", "Himalayas", "Vindhya", "Satpura"),
                        correctIndex = 0,
                        explanationBengali = "আরাবল্লী প্রাক-ক্যাম্ব্রিয়ান যুগে গঠিত ভারতের প্রাচীনতম ক্ষয়প্রাপ্ত ভঙ্গিল পর্বত।",
                        explanationEnglish = "Aravalli is an ancient residual fold mountain range."
                    ),
                    LiveQuizItem(
                        id = "geo_9",
                        questionBengali = "ভারতের কোন রাজ্যকে 'ভারতের মশলার বাগান' বলা হয়?",
                        questionEnglish = "Which state is known as the 'Spice Garden of India'?",
                        optionsBengali = listOf("কেরালা", "অসম", "পশ্চিমবঙ্গ", "পাঞ্জাব"),
                        optionsEnglish = listOf("Kerala", "Assam", "West Bengal", "Punjab"),
                        correctIndex = 0,
                        explanationBengali = "এলাচ, গোলমরিচ, লবঙ্গ ইত্যাদি উৎপাদনের কারণে কেরালাকে মশলার বাগান বলা হয়।",
                        explanationEnglish = "Kerala has historically produced high qualities of exotic spices."
                    ),
                    LiveQuizItem(
                        id = "geo_10",
                        questionBengali = "উপগ্রহ চিত্রে উদ্ভিদের উপস্থিতি কোন রঙে দেখানো হয়?",
                        questionEnglish = "In standard false-color composite satellite images, vegetation appears as which color?",
                        optionsBengali = listOf("লাল ", "সবুজ ", "নীল ", "হলুদ "),
                        optionsEnglish = listOf("Red", "Green", "Blue", "Yellow"),
                        correctIndex = 0,
                        explanationBengali = "FCC (False Color Composite) চিত্রে অবলোহিত রশ্মি প্রতিফলনের কারণে গাছপালা উজ্জ্বল লাল দেখায়।",
                        explanationEnglish = "Chlorophyll reflects near-infrared, causing vegetation to render red in FCC imagery."
                    )
                )
            ),
            LiveSubjectArena(
                id = "life_science",
                titleBengali = "জীবন বিজ্ঞান",
                titleEnglish = "Life Science Arena",
                subtitleBengali = "হরমোন, জনন ও পরিবেশ",
                subtitleEnglish = "Hormones, Genetics & Environment",
                icon = Icons.Default.Biotech,
                primaryColor = Color(0xFF10B981),
                onlineCount = 430,
                questions = listOf(
                    LiveQuizItem(
                        id = "ls_1",
                        questionBengali = "উদ্ভিদের আলোকবর্তী চলন নিয়ন্ত্রণকারী প্রধান হরমোন কোনটি?",
                        questionEnglish = "Which hormone regulates phototropic movement in plants?",
                        optionsBengali = listOf("জিব্বেরেলিন", "অক্সিন", "সাইটোকাইনিন", "ইথিলিন"),
                        optionsEnglish = listOf("Gibberellin", "Auxin", "Cytokinin", "Ethylene"),
                        correctIndex = 1,
                        explanationBengali = "অক্সিন উদ্ভিদের কাণ্ডের অগ্রভাগে আলোর বিপরীত দিকে সঞ্চিত হয়ে আলোকের দিকে বাঁকতে সাহায্য করে।",
                        explanationEnglish = "Auxin promotes cell elongation towards light source."
                    ),
                    LiveQuizItem(
                        id = "ls_2",
                        questionBengali = "মানবদেহের শুক্রাণুতে অবস্থিত অটোজোমের সংখ্যা কত?",
                        questionEnglish = "How many autosomes are present in human sperm?",
                        optionsBengali = listOf("২৩টি", "২২টি", "৪৪টি", "৪৬টি"),
                        optionsEnglish = listOf("23", "22", "44", "46"),
                        correctIndex = 1,
                        explanationBengali = "শুক্রাণুতে মোট ২৩টি ক্রোমোজোম থাকে, যার মধ্যে ২২টি অটোজোম ও ১টি সেক্স ক্রোমোজোম (X বা Y)।",
                        explanationEnglish = "Human sperm has 22 autosomes and 1 allosome."
                    ),
                    LiveQuizItem(
                        id = "ls_3",
                        questionBengali = "বংশগতির জনক কাকে বলা হয়?",
                        questionEnglish = "Who is known as the Father of Genetics?",
                        optionsBengali = listOf("চার্লস ডারউইন", "গ্রেগর জোহান মেন্ডেল", "ল্যামার্ক", "দ্য ভ্রিস"),
                        optionsEnglish = listOf("Charles Darwin", "Gregor Johann Mendel", "Lamarck", "Hugo de Vries"),
                        correctIndex = 1,
                        explanationBengali = "মটর গাছের ওপর দীর্ঘ পরীক্ষার মাধ্যমে মেন্ডেল বংশগতির সূত্র আবিষ্কার করেন।",
                        explanationEnglish = "Mendel formulated the laws of inheritance through pea plant experiments."
                    ),
                    LiveQuizItem(
                        id = "ls_4",
                        questionBengali = "সালোকসংশ্লেষের প্রধান রঞ্জক কোনটি?",
                        questionEnglish = "Which is the primary pigment of photosynthesis?",
                        optionsBengali = listOf("ক্লোরোফিল-a", "ক্লোরোফিল-b", "ক্যারোটিন", "জ্যান্থোফিল"),
                        optionsEnglish = listOf("Chlorophyll-a", "Chlorophyll-b", "Carotene", "Xanthophyll"),
                        correctIndex = 0,
                        explanationBengali = "ক্লোরোফিল-a সরাসরি সৌরশক্তি শোষণ করে বিক্রিয়া কেন্দ্রে পাঠায়।",
                        explanationEnglish = "Chlorophyll-a acts as the primary reaction center pigment."
                    ),
                    LiveQuizItem(
                        id = "ls_5",
                        questionBengali = "মানবদেহের কোন অঙ্গকে 'রক্তের প্রাকৃতিক ছাঁকনি' বলা হয়?",
                        questionEnglish = "Which organ acts as the natural blood filter in humans?",
                        optionsBengali = listOf("যকৃত ", "হৃদপিণ্ড ", "বৃক্ক ", "ফুসফুস "),
                        optionsEnglish = listOf("Liver", "Heart", "Kidney", "Lungs"),
                        correctIndex = 2,
                        explanationBengali = "নেফ্রনের গ্লোমেরুলাসের মাধ্যমে বৃক্কে রক্ত পরিস্রুত হয়ে নাইট্রোজেনঘটিত বর্জ্য অপসারিত হয়।",
                        explanationEnglish = "Kidneys filter nitrogenous waste from the bloodstream via nephrons."
                    ),
                    LiveQuizItem(
                        id = "ls_6",
                        questionBengali = "ডিএনএ -এর দ্বিতন্ত্রী কাঠামোর মডেল কে আবিষ্কার করেন?",
                        questionEnglish = "Who proposed the double-helix model of DNA?",
                        optionsBengali = listOf("ওয়াটসন ও ক্রিক", "শ্লেডেন ও সোয়ান", "রবার্ট হুক", "হরগোবিন্দ খোরানা"),
                        optionsEnglish = listOf("Watson and Crick", "Schleiden and Schwann", "Robert Hooke", "Har Gobind Khorana"),
                        correctIndex = 0,
                        explanationBengali = "১৯৫৩ সালে জেমস ওয়াটসন এবং ফ্রান্সিস ক্রিক ডিএনএর দ্বিতন্ত্রী প্যাঁচানো সিঁড়ির মতো মডেল প্রকাশ করেন।",
                        explanationEnglish = "Watson and Crick proposed the DNA double helix in 1953."
                    ),
                    LiveQuizItem(
                        id = "ls_7",
                        questionBengali = "মাছের দেহে প্লবতা নিয়ন্ত্রণকারী বিশেষ অঙ্গটি কোনটি?",
                        questionEnglish = "Which organ controls buoyancy in bony fishes?",
                        optionsBengali = listOf("ফুলকা", "পার্শ্বরেখা", "পটকা ", "পৃষ্ঠ পাখনা"),
                        optionsEnglish = listOf("Gills", "Lateral line", "Swim Bladder", "Dorsal Fin"),
                        correctIndex = 2,
                        explanationBengali = "পটকার ভেতরে গ্যাস ভরাট বা খালি করার মাধ্যমে মাছ জলের বিভিন্ন গভীরতায় ওঠানামা করে।",
                        explanationEnglish = "The swim bladder adjusts buoyancy by regulating internal gas pressure."
                    ),
                    LiveQuizItem(
                        id = "ls_8",
                        questionBengali = "উড়বার সুবিধার জন্য পায়রার ফুসফুসের সাথে কয়টি বায়ুথলি থাকে?",
                        questionEnglish = "How many air sacs are attached to a pigeon's lungs?",
                        optionsBengali = listOf("৭টি", "৯টি", "১১টি", "৫টি"),
                        optionsEnglish = listOf("7", "9", "11", "5"),
                        correctIndex = 1,
                        explanationBengali = "পায়রার দেহে ৯টি বায়ুথলি থাকে যা দেহকে হালকা রাখতে ও দ্বিশ্বসন প্রক্রিয়ায় সাহায্য করে।",
                        explanationEnglish = "Pigeons possess 9 air sacs for double respiration and weight reduction."
                    ),
                    LiveQuizItem(
                        id = "ls_9",
                        questionBengali = "কোষ বিভাজনের কোন দশায় ক্রোমোজোমগুলি বিষুব অঞ্চলে সুসজ্জিত হয়?",
                        questionEnglish = "In which stage of cell division do chromosomes align at the equatorial plate?",
                        optionsBengali = listOf("প্রোফেজ", "মেটাফেজ", "অ্যানাফেজ", "টেলোফেজ"),
                        optionsEnglish = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                        correctIndex = 1,
                        explanationBengali = "মেটাফেজ দশায় ক্রোমোজোমগুলি সবচেয়ে স্পষ্ট হয় এবং নিরক্ষীয় তলে মেটাফেজ প্লেট গঠন করে।",
                        explanationEnglish = "Metaphase features chromosomes aligned along the equatorial plane."
                    ),
                    LiveQuizItem(
                        id = "ls_10",
                        questionBengali = "সুন্দরবনের শ্বাসমূল কোন ধরনের উদ্ভিদে দেখা যায়?",
                        questionEnglish = "Pneumatophores are characteristic of which plants in Sundarbans?",
                        optionsBengali = listOf("জেরোফাইট", "হাইড্রোফাইট", "হ্যালোফাইট ", "মেসোফাইট"),
                        optionsEnglish = listOf("Xerophytes", "Hydrophytes", "Halophytes", "Mesophytes"),
                        correctIndex = 2,
                        explanationBengali = "লবণাক্ত কাদা মাটিতে অক্সিজেনের অভাব পূরণের জন্য সুন্দরী, গরান ইত্যাদি উদ্ভিদে শ্বাসমূল বের হয়।",
                        explanationEnglish = "Halophytes produce respiratory roots to intake oxygen in waterlogged saline soil."
                    )
                )
            ),
            LiveSubjectArena(
                id = "physical_science",
                titleBengali = "ভৌত বিজ্ঞান",
                titleEnglish = "Physical Science Arena",
                subtitleBengali = "আলো, তড়িৎ, পর্যায় সারণি ও গ্যাস",
                subtitleEnglish = "Optics, Electricity & Periodic Table",
                icon = Icons.Default.Science,
                primaryColor = Color(0xFF3B82F6),
                onlineCount = 490,
                questions = listOf(
                    LiveQuizItem(
                        id = "ps_1",
                        questionBengali = "চার্লসের সূত্রে কোন রাশি দুটি ধ্রুবক থাকে?",
                        questionEnglish = "Which two variables remain constant in Charles's Law?",
                        optionsBengali = listOf("চাপ ও আয়তন", "গ্যাসের ভর ও চাপ", "ভর ও তাপমাত্রা", "উষ্ণতা ও চাপ"),
                        optionsEnglish = listOf("Pressure & Volume", "Gas Mass & Pressure", "Mass & Temperature", "Temp & Pressure"),
                        correctIndex = 1,
                        explanationBengali = "স্থির চাপে নির্দিষ্ট ভরের যেকোনো গ্যাসের আয়তন প্রতি ডিগ্রি সেলসিয়াস উষ্ণতা পরিবর্তনে ১/২৭৩ অংশ হ্রাস বা বৃদ্ধি পায়।",
                        explanationEnglish = "In Charles's law, mass and pressure of the gas remain constant."
                    ),
                    LiveQuizItem(
                        id = "ps_2",
                        questionBengali = "STP-তে 1 মোল যেকোনো গ্যাসের আয়তন কত?",
                        questionEnglish = "What is the molar volume of any gas at STP?",
                        optionsBengali = listOf("11.2 লিটার", "22.4 লিটার", "44.8 লিটার", "33.6 লিটার"),
                        optionsEnglish = listOf("11.2 Liters", "22.4 Liters", "44.8 Liters", "33.6 Liters"),
                        correctIndex = 1,
                        explanationBengali = "আভোগাড্রো প্রকল্প অনুসারে প্রমাণ চাপ ও উষ্ণতায় ১ মোল গ্যাসের আয়তন ২২.৪ লিটার।",
                        explanationEnglish = "Standard molar gas volume at STP is 22.4 liters."
                    ),
                    LiveQuizItem(
                        id = "ps_3",
                        questionBengali = "দিনের বেলা আকাশ নীল দেখানোর প্রধান কারণ আলোর কোন ধর্ম?",
                        questionEnglish = "What optical phenomenon causes the sky to appear blue?",
                        optionsBengali = listOf("প্রতিসরণ", "প্রতিফলন", "বিক্ষেপণ ", "বিচ্ছুরণ"),
                        optionsEnglish = listOf("Refraction", "Reflection", "Scattering", "Dispersion"),
                        correctIndex = 2,
                        explanationBengali = "রেলের বিক্ষেপণ সূত্র অনুযায়ী নীল আলোর তরঙ্গদৈর্ঘ্য কম হওয়ায় বায়ুমণ্ডলে এটি সবচেয়ে বেশি ছড়িয়ে পড়ে।",
                        explanationEnglish = "Rayleigh scattering of shorter wavelength blue light gives the sky its color."
                    ),
                    LiveQuizItem(
                        id = "ps_4",
                        questionBengali = "তড়িৎ রোধের SI একক কোনটি?",
                        questionEnglish = "What is the SI unit of Electrical Resistance?",
                        optionsBengali = listOf("ভোল্ট ", "অ্যাম্পিয়ার ", "ওহম ", "ওয়াট "),
                        optionsEnglish = listOf("Volt", "Ampere", "Ohm", "Watt"),
                        correctIndex = 2,
                        explanationBengali = "ওহমের সূত্রানুযায়ী R = V / I, যার একক ওহম (Ω)।",
                        explanationEnglish = "The SI unit of resistance is the Ohm (Ω)."
                    ),
                    LiveQuizItem(
                        id = "ps_5",
                        questionBengali = "আধুনিক পর্যায় সারণিতে সবচেয়ে হালকা নোবেল গ্যাস কোনটি?",
                        questionEnglish = "Which is the lightest noble gas in the periodic table?",
                        optionsBengali = listOf("নিয়ন (Ne)", "হিলিয়াম (He)", "আর্গন (Ar)", "ক্রিপ্টন (Kr)"),
                        optionsEnglish = listOf("Neon", "Helium", "Argon", "Krypton"),
                        correctIndex = 1,
                        explanationBengali = "হিলিয়ামের পারমাণবিক সংখ্যা ২ এবং এটি ১৮ নম্বর শ্রেণির শীর্ষে অবস্থিত।",
                        explanationEnglish = "Helium (atomic number 2) is the lightest noble gas."
                    ),
                    LiveQuizItem(
                        id = "ps_6",
                        questionBengali = "নিচের কোন যৌগটি সাধারণ উষ্ণতায় তড়িৎযোজী ?",
                        questionEnglish = "Which of the following compounds exhibits ionic bonding?",
                        optionsBengali = listOf("NaCl (খাবার লবণ)", "CH₄ (মিথেন)", "H₂O (জল)", "CO₂ (কার্বন ডাই অক্সাইড)"),
                        optionsEnglish = listOf("NaCl", "CH4", "H2O", "CO2"),
                        correctIndex = 0,
                        explanationBengali = "সোডিয়াম (Na+) ও ক্লোরাইড (Cl-) তড়িৎ-আকর্ষণ বলের দ্বারা যুক্ত হয়ে NaCl তৈরি করে।",
                        explanationEnglish = "Sodium chloride is composed of ionic lattice structure."
                    ),
                    LiveQuizItem(
                        id = "ps_7",
                        questionBengali = "বায়ুমণ্ডলের ওজোন স্তর ধ্বংসের জন্য কৃত্রিম প্রধান দায়ী যৌগ কোনটি?",
                        questionEnglish = "Which artificial chemical is mainly responsible for ozone depletion?",
                        optionsBengali = listOf("CO₂", "CFC (ক্লোরোফ্লুরোকার্বন)", "CH₄", "SO₂"),
                        optionsEnglish = listOf("CO2", "CFC", "CH4", "SO2"),
                        correctIndex = 1,
                        explanationBengali = "CFC থেকে মুক্ত ক্লোরিন পরমাণু হাজার হাজার ওজোন অণুকে ভেঙে অক্সিজেনে পরিণত করে।",
                        explanationEnglish = "Chlorofluorocarbons release free chlorine radicals that destroy ozone."
                    ),
                    LiveQuizItem(
                        id = "ps_8",
                        questionBengali = "তেজস্ক্রিয় রশ্মির মধ্যে ধনাত্মক আধানযুক্ত কণা কোনটি?",
                        questionEnglish = "Which radioactive radiation consists of positively charged particles?",
                        optionsBengali = listOf("আলফা (α) কণা", "বিটা (β) কণা", "গামা (γ) রশ্মি", "এক্স রশ্মি"),
                        optionsEnglish = listOf("Alpha particle", "Beta particle", "Gamma ray", "X-ray"),
                        correctIndex = 0,
                        explanationBengali = "আলফা কণা হলো দ্বিপজিটিভ হিলিয়াম নিউক্লিয়াস (He²⁺)।",
                        explanationEnglish = "Alpha particles carry a +2 elementary charge."
                    ),
                    LiveQuizItem(
                        id = "ps_9",
                        questionBengali = "একটি অবতল দর্পণের বক্রতা ব্যাসার্ধ 20 সেমি হলে তার ফোকাস দৈর্ঘ্য কত?",
                        questionEnglish = "What is the focal length of a concave mirror having radius of curvature 20 cm?",
                        optionsBengali = listOf("40 সেমি", "10 সেমি", "5 সেমি", "20 সেমি"),
                        optionsEnglish = listOf("40 cm", "10 cm", "5 cm", "20 cm"),
                        correctIndex = 1,
                        explanationBengali = "ফোকাস দৈর্ঘ্য f = R / 2 = 20 / 2 = 10 সেমি।",
                        explanationEnglish = "Focal length f = R/2, so 20/2 = 10 cm."
                    ),
                    LiveQuizItem(
                        id = "ps_10",
                        questionBengali = "BOT  ইউনিট দিয়ে কোন রাশি পরিমাপ করা হয়?",
                        questionEnglish = "Which electrical quantity is measured in B.O.T. unit?",
                        optionsBengali = listOf("তড়িৎ প্রবাহ", "তড়িৎ শক্তি (Electrical Energy)", "তড়িৎ বিভব", "রোধ"),
                        optionsEnglish = listOf("Current", "Electrical Energy", "Voltage", "Resistance"),
                        correctIndex = 1,
                        explanationBengali = "১ BOT ইউনিট = ১ কিলোওয়াট-ঘণ্টা (kWh) যা বাণিজ্যিক তড়িৎ শক্তির একক।",
                        explanationEnglish = "1 BOT unit equals 1 kilowatt-hour of consumed electrical energy."
                    )
                )
            ),
            LiveSubjectArena(
                id = "mathematics",
                titleBengali = "গণিত",
                titleEnglish = "Mathematics Arena",
                subtitleBengali = "বীজগণিত, জ্যামিতি, পরিমিতি ও ত্রিকোণমিতি",
                subtitleEnglish = "Algebra, Geometry & Trigonometry",
                icon = Icons.Default.Calculate,
                primaryColor = Color(0xFFEF4444),
                onlineCount = 410,
                questions = listOf(
                    LiveQuizItem(
                        id = "math_1",
                        questionBengali = "ax² + bx + c = 0 সমীকরণের বীজ দুটি বাস্তব ও সমান হওয়ার শর্ত কী?",
                        questionEnglish = "What is the condition for real and equal roots in ax² + bx + c = 0?",
                        optionsBengali = listOf("b² - 4ac = 0", "b² - 4ac > 0", "b² - 4ac < 0", "b = ac"),
                        optionsEnglish = listOf("b² - 4ac = 0", "b² - 4ac > 0", "b² - 4ac < 0", "b = ac"),
                        correctIndex = 0,
                        explanationBengali = "নিরূপক D = b² - 4ac এর মান শূন্য (০) হলে দ্বিঘাত সমীকরণের বীজদ্বয় বাস্তব ও সমান হয়।",
                        explanationEnglish = "The discriminant must equal zero for real and identical roots."
                    ),
                    LiveQuizItem(
                        id = "math_2",
                        questionBengali = "কোনো বৃত্তের স্পর্শক এবং স্পর্শবিন্দুগামী ব্যাসার্ধের মধ্যবর্তী কোণ কত ডিগ্রি?",
                        questionEnglish = "What is the angle between a tangent to a circle and the radius through the point of contact?",
                        optionsBengali = listOf("৯০° (এক সমকোণ)", "৪৫°", "৬০°", "১৮০°"),
                        optionsEnglish = listOf("90° (Right angle)", "45°", "60°", "180°"),
                        correctIndex = 0,
                        explanationBengali = "উপপাদ্য অনুসারে স্পর্শবিন্দুগামী ব্যাসার্ধ স্পর্শকের ওপর সর্বদা লম্ব (৯০°) হয়।",
                        explanationEnglish = "A radius drawn to the point of tangency is perpendicular (90°) to the tangent."
                    ),
                    LiveQuizItem(
                        id = "math_3",
                        questionBengali = "r ব্যাসার্ধবিশিষ্ট একটি নিরেট গোলকের সমগ্রতলের ক্ষেত্রফল কত?",
                        questionEnglish = "What is the total surface area of a solid sphere of radius r?",
                        optionsBengali = listOf("4πr² বর্গ একক", "2πr²", "3πr²", "4/3 πr³"),
                        optionsEnglish = listOf("4πr²", "2πr²", "3πr²", "4/3 πr³"),
                        correctIndex = 0,
                        explanationBengali = "গোলকের পৃষ্ঠতলের ক্ষেত্রফল ৪টি বৃত্তের ক্ষেত্রফলের সমান অর্থাৎ 4πr²।",
                        explanationEnglish = "Total surface area of a sphere of radius r is 4πr²."
                    ),
                    LiveQuizItem(
                        id = "math_4",
                        questionBengali = "ত্রিকোণমিতিতে sin²θ + cos²θ এর মান সর্বদা কত?",
                        questionEnglish = "In trigonometry, what is the constant value of sin²θ + cos²θ?",
                        optionsBengali = listOf("১", "০", "-১", "২"),
                        optionsEnglish = listOf("1", "0", "-1", "2"),
                        correctIndex = 0,
                        explanationBengali = "পিথাগোরাসের উপপাদ্য অনুযায়ী লম্ব² + ভূমি² = অতিভুজ², যা ভাগ করলে মান সর্বদা ১ হয়।",
                        explanationEnglish = "The fundamental Pythagorean trigonometric identity equals 1."
                    ),
                    LiveQuizItem(
                        id = "math_5",
                        questionBengali = "a বাহুবিশিষ্ট একটি ঘনকের কর্ণের দৈর্ঘ্য  কত?",
                        questionEnglish = "What is the length of the body diagonal of a cube with side a?",
                        optionsBengali = listOf("√3 a", "√2 a", "2a", "3a"),
                        optionsEnglish = listOf("√3 a", "√2 a", "2a", "3a"),
                        correctIndex = 0,
                        explanationBengali = "ঘনকের কর্ণের সূত্র = √(a² + a² + a²) = √(3a²) = √3 a।",
                        explanationEnglish = "Body diagonal of a cube with side length a is √3 a."
                    ),
                    LiveQuizItem(
                        id = "math_6",
                        questionBengali = "বার্ষিক r% সরল সুদে p টাকার t বছরের সুদ I হলে সঠিক সম্পর্ক কোনটি?",
                        questionEnglish = "If simple interest is I for principal p at rate r% for time t, what is the relation?",
                        optionsBengali = listOf("I = (p × r × t) / 100", "p = I × r × t", "I = 100 / (prt)", "r = (I × p) / 100"),
                        optionsEnglish = listOf("I = (p × r × t) / 100", "p = I × r × t", "I = 100 / (prt)", "r = (I × p) / 100"),
                        correctIndex = 0,
                        explanationBengali = "সরল সুদের মূল সূত্র I = prt / 100।",
                        explanationEnglish = "Simple interest formula: I = prt / 100."
                    ),
                    LiveQuizItem(
                        id = "math_7",
                        questionBengali = "2, 4, 6, 8, 10 সংখ্যাগুলির যৌগিক গড়  কত?",
                        questionEnglish = "What is the arithmetic mean of 2, 4, 6, 8, 10?",
                        optionsBengali = listOf("৬", "৫", "৭", "৮"),
                        optionsEnglish = listOf("6", "5", "7", "8"),
                        correctIndex = 0,
                        explanationBengali = "মোট যোগফল = ৩০, সংখ্যা = ৫টি, সুতরাং গড় = ৩০ / ৫ = ৬।",
                        explanationEnglish = "Sum = 30; Count = 5; Mean = 30 / 5 = 6."
                    ),
                    LiveQuizItem(
                        id = "math_8",
                        questionBengali = "যেকোনো বৃত্তস্থ চতুর্ভুজের বিপরীত কোণদ্বয় পরস্পরের কী?",
                        questionEnglish = "Opposite angles of a cyclic quadrilateral are always:",
                        optionsBengali = listOf("সম্পূরক (যোগফল ১৮০°)", "পূরক (যোগফল ৯০°)", "সমান", "বিপ্রতীপ"),
                        optionsEnglish = listOf("Supplementary (180°)", "Complementary (90°)", "Equal", "Vertically opposite"),
                        correctIndex = 0,
                        explanationBengali = "বৃত্তস্থ চতুর্ভুজের যেকোনো দুটি বিপরীত কোণের সমষ্টি দুই সমকোণ বা ১৮০°।",
                        explanationEnglish = "Opposite interior angles of a cyclic quadrilateral sum up to 180°."
                    ),
                    LiveQuizItem(
                        id = "math_9",
                        questionBengali = "tan 45° এর সঠিক মান কত?",
                        questionEnglish = "What is the exact value of tan 45°?",
                        optionsBengali = listOf("১", "০", "১/√৩", "√৩"),
                        optionsEnglish = listOf("1", "0", "1/√3", "√3"),
                        correctIndex = 0,
                        explanationBengali = "৪৫° কোণের সমদ্বিবাহু সমকোণী ত্রিভুজে লম্ব = ভূমি, তাই tan 45° = ১।",
                        explanationEnglish = "Since opposite equals adjacent in a 45°-45°-90° triangle, tan 45° = 1."
                    ),
                    LiveQuizItem(
                        id = "math_10",
                        questionBengali = "বৃত্তের অর্ধবৃত্তস্থ কোণের মান কত ডিগ্রি?",
                        questionEnglish = "What is the measure of an angle inscribed in a semicircle?",
                        optionsBengali = listOf("৯০° (এক সমকোণ)", "৬০°", "৪৫°", "১২০°"),
                        optionsEnglish = listOf("90° (Right angle)", "60°", "45°", "120°"),
                        correctIndex = 0,
                        explanationBengali = "কেন্দ্রস্থ সরলকোণ ১৮০° এর অর্ধেক হওয়ায় পরিধিস্থ কোণটি সর্বদা ৯০° হয়।",
                        explanationEnglish = "Thales's theorem proves any angle inscribed in a semicircle equals 90°."
                    )
                )
            ),
            LiveSubjectArena(
                id = "mega_grand",
                titleBengali = "👑 অল-বেঙ্গল মেগা গ্র্যান্ড কুইজ",
                titleEnglish = "All-Bengal Mega Championship",
                subtitleBengali = "সব বিষয়ের সেরা ১০টি চ্যাম্পিয়নশিপ প্রশ্ন",
                subtitleEnglish = "Mixed Subject Grand Tournament",
                icon = Icons.Default.EmojiEvents,
                primaryColor = ExamGold,
                onlineCount = 1250,
                questions = listOf(
                    LiveQuizItem(
                        id = "mg_1",
                        questionBengali = "সালোকসংশ্লেষের জন্য প্রয়োজনীয় আলোক শক্তি কোন অঙ্গাণু দ্বারা শোষিত হয়?",
                        questionEnglish = "Which organelle absorbs the light energy required for photosynthesis?",
                        optionsBengali = listOf("ক্লোরোপ্লাস্ট (Chloroplast)", "মাইটোকনড্রিয়া", "রাইবোজোম", "গলগি বডি"),
                        optionsEnglish = listOf("Chloroplast", "Mitochondria", "Ribosome", "Golgi Body"),
                        correctIndex = 0,
                        explanationBengali = "ক্লোরোপ্লাস্টের থাইলাকয়েডে অবস্থিত ক্লোরোফিল সৌরশক্তি শোষণ করে রাসায়নিক শক্তিতে রূপান্তর করে।",
                        explanationEnglish = "Chloroplast captures solar photons via chlorophyll pigments."
                    ),
                    LiveQuizItem(
                        id = "mg_2",
                        questionBengali = "বায়ুমণ্ডলে ওজোন স্তর  প্রধানত কোন স্তরে অবস্থিত?",
                        questionEnglish = "In which atmospheric layer is the Ozone Layer predominantly located?",
                        optionsBengali = listOf("স্ট্র্যাটোস্ফিয়ার", "ট্রপোস্ফিয়ার", "মেসোস্ফিয়ার", "থার্মোস্ফিয়ার"),
                        optionsEnglish = listOf("Stratosphere", "Troposphere", "Mesosphere", "Thermosphere"),
                        correctIndex = 0,
                        explanationBengali = "স্ট্র্যাটোস্ফিয়ারে ওজোন গ্যাসের ঘনত্ব বেশি থাকে যা ক্ষতিকর অতিবেগুনি রশ্মি শোষণ করে।",
                        explanationEnglish = "The stratosphere houses the protective ozone shield."
                    ),
                    LiveQuizItem(
                        id = "mg_3",
                        questionBengali = "কোন হরমোনকে মানবদেহের 'আপৎকালীন হরমোন'  বলা হয়?",
                        questionEnglish = "Which hormone is known as the 'Emergency Hormone' in human body?",
                        optionsBengali = listOf("অ্যাড্রিনালিন", "ইনসুলিন", "থাইরক্সিন", "ইস্ট্রোজেন"),
                        optionsEnglish = listOf("Adrenaline", "Insulin", "Thyroxine", "Estrogen"),
                        correctIndex = 0,
                        explanationBengali = "ভয়, রাগ বা উত্তেজনার সময় অ্যাড্রিনাল গ্রন্থি থেকে দ্রুত ক্ষরিত হয়ে দেহকে প্রস্তুত করে।",
                        explanationEnglish = "Adrenaline prepares organism systems for emergency responses."
                    ),
                    LiveQuizItem(
                        id = "mg_4",
                        questionBengali = "পর্যায় সারণিতে সবচেয়ে তড়িৎ-ঋণাত্মক  মৌল কোনটি?",
                        questionEnglish = "Which is the most electronegative element in the Periodic Table?",
                        optionsBengali = listOf("ফ্লোরিন ", "ক্লোরিন ", "অক্সিজেন ", "নাইট্রোজেন "),
                        optionsEnglish = listOf("Fluorine ", "Chlorine ", "Oxygen ", "Nitrogen "),
                        correctIndex = 0,
                        explanationBengali = "পাউলিং স্কেলে ফ্লোরিনের তড়িৎ-ঋণাত্মকতার মান সর্বোচ্চ (৪.০)।",
                        explanationEnglish = "Fluorine possesses the maximum electronegativity rating of 4.0."
                    ),
                    LiveQuizItem(
                        id = "mg_5",
                        questionBengali = "ভারতের কোন বায়োস্ফিয়ার রিজার্ভ রয়েল বেঙ্গল টাইগারের বিশ্ববিখ্যাত বাসস্থান?",
                        questionEnglish = "Which Biosphere Reserve in India is world-famous for Royal Bengal Tigers?",
                        optionsBengali = listOf("সুন্দরবন (Sundarbans)", "মানস", "কাজিরাঙা", "সিমলিপাল"),
                        optionsEnglish = listOf("Sundarbans", "Manas", "Kaziranga", "Simlipal"),
                        correctIndex = 0,
                        explanationBengali = "পশ্চিমবঙ্গের সুন্দরবন ম্যানগ্রোভ অরণ্য এবং রয়েল বেঙ্গল টাইগারের প্রাকৃতিক বাসভূমি।",
                        explanationEnglish = "Sundarbans is renowned as the primary delta habitat of Royal Bengal Tigers."
                    ),
                    LiveQuizItem(
                        id = "mg_6",
                        questionBengali = "ax² + bx + c = 0 সমীকরণের বীজ দুটি পরস্পর অন্যোন্যক  হওয়ার শর্ত কী?",
                        questionEnglish = "What is the condition that roots of ax² + bx + c = 0 are reciprocals of each other?",
                        optionsBengali = listOf("c = a", "b = a", "c = 0", "b = 0"),
                        optionsEnglish = listOf("c = a", "b = a", "c = 0", "b = 0"),
                        correctIndex = 0,
                        explanationBengali = "বীজদ্বয়ের গুণফল α × (1/α) = 1 = c / a, সুতরাং c = a।",
                        explanationEnglish = "Product of reciprocal roots is 1 = c/a, hence c = a."
                    ),
                    LiveQuizItem(
                        id = "mg_7",
                        questionBengali = "১৮৫৭ সালের মহাবিদ্রোহের সময় ভারতের মুঘল সম্রাট কে ছিলেন?",
                        questionEnglish = "Who was the Mughal Emperor of India during the 1857 Revolt?",
                        optionsBengali = listOf("দ্বিতীয় বাহাদুর শাহ", "আকবর শাহ", "শাহ আলম", "জাহান্দার শাহ"),
                        optionsEnglish = listOf("Bahadur Shah II", "Akbar Shah", "Shah Alam", "Jahandar Shah"),
                        correctIndex = 0,
                        explanationBengali = "বিদ্রোহী সিপাহীরা বয়োবৃদ্ধ বাহাদুর শাহ জাফরকে ভারতের সম্রাট ঘোষণা করেছিলেন।",
                        explanationEnglish = "Bahadur Shah Zafar was proclaimed Emperor by the insurgents."
                    ),
                    LiveQuizItem(
                        id = "mg_8",
                        questionBengali = "'জ্ঞানচক্ষু' গল্পে তপনের লেখা গল্পটি কোন পত্রিকায় ছাপানো হয়েছিল?",
                        questionEnglish = "In the story 'Gyanchakshu', in which magazine was Tapan's story published?",
                        optionsBengali = listOf("সন্ধ্যাতারা", "শুকতারা", "আনন্দমেলা", "কিশোর ভারতী"),
                        optionsEnglish = listOf("Sandhyatara", "Shuktara", "Anandamela", "Kishore Bharati"),
                        correctIndex = 0,
                        explanationBengali = "ছোটোমেসোর সহায়তায় তপনের 'প্রথম দিন' গল্পটি সন্ধ্যাতারা পত্রিকায় প্রকাশিত হয়।",
                        explanationEnglish = "Tapan's story appeared in the prestigious Sandhyatara magazine."
                    ),
                    LiveQuizItem(
                        id = "mg_9",
                        questionBengali = "দক্ষিণ ভারতের গঙ্গা বলা হয় কোন নদীকে?",
                        questionEnglish = "Which river is often called the 'Ganga of South India'?",
                        optionsBengali = listOf("গোদাবরী", "কৃষ্ণা", "কাবেরী", "নর্মদা"),
                        optionsEnglish = listOf("Godavari", "Krishna", "Kaveri", "Narmada"),
                        correctIndex = 0,
                        explanationBengali = "দৈর্ঘ্য ও ধর্মীয় গুরুত্বের কারণে গোদাবরী নদীকে দক্ষিণ ভারতের গঙ্গা বা বৃদ্ধগঙ্গা বলা হয়।",
                        explanationEnglish = "Godavari is termed the Dakshin Ganga due to its length and cultural stature."
                    ),
                    LiveQuizItem(
                        id = "mg_10",
                        questionBengali = "একটি সমকোণী সমদ্বিবাহু ত্রিভুজের সূক্ষ্মকোণ দুটির প্রত্যেকটির মান কত?",
                        questionEnglish = "What is the measure of each acute angle in an isosceles right-angled triangle?",
                        optionsBengali = listOf("৪৫°", "৩০°", "৬০°", "৫০°"),
                        optionsEnglish = listOf("45°", "30°", "60°", "50°"),
                        correctIndex = 0,
                        explanationBengali = "একটি কোণ ৯০° এবং অবশিষ্ট দুটি সমান কোণ (১৮০ - ৯০) / ২ = ৪৫°।",
                        explanationEnglish = "Since the two non-right angles are congruent, each measures (180 - 90)/2 = 45°."
                    )
                )
            )
        )
    }

    // Selected arena state
    var selectedArena by remember { mutableStateOf<LiveSubjectArena?>(null) }

    // Navigation and game states: LOBBY, DETAILS, COUNTDOWN, PLAYING, RESULT
    var gameState by remember { mutableStateOf("LOBBY") }
    var countdownSec by remember { mutableIntStateOf(3) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var userScore by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    // 1 MINUTE (60 SECONDS) PER QUESTION as requested!
    var questionTimer by remember { mutableIntStateOf(60) }
    var hasRewardedBonus by remember { mutableStateOf(false) }

    // Pulsating animation for the top LIVE red dot
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_scale"
    )

    // Distinct pre-numbers (base player count) for each subject arena
    val subjectPreNumbers = remember {
        mapOf(
            "bengali" to 500,
            "english" to 420,
            "history" to 340,
            "geography" to 280,
            "life_science" to 390,
            "physical_science" to 440,
            "mathematics" to 470,
            "mega_grand" to 850
        )
    }

    // Dynamic fluctuating online player count per arena (Strictly 50 Minimum, 100 Maximum continually changing)
    val onlineCountsMap = remember {
        mutableStateMapOf<String, Int>().apply {
            arenas.forEach { arena ->
                put(arena.id, (55..85).random())
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1200L) // Continually updates smoothly every 1.2s
            arenas.forEach { arena ->
                if (kotlin.random.Random.nextFloat() < 0.70f) {
                    val current = onlineCountsMap[arena.id] ?: 75
                    val delta = (-2..3).random()
                    // Strictly minimum 50, maximum 100 as requested!
                    val nextCount = (current + delta).coerceIn(50, 100)
                    onlineCountsMap[arena.id] = nextCount
                }
            }
        }
    }

    fun formatBengaliDigits(num: Int): String {
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")
    }

    // Helper to extract all available questions for the selected subject from the total database question bank
    fun buildSubjectPool(
        arenaId: String,
        curatedFallback: List<LiveQuizItem>,
        allDbQuestions: List<QuestionEntity>
    ): List<LiveQuizItem> {
        val matchingEntities = allDbQuestions.filter { q ->
            when (arenaId) {
                "bengali" -> q.subjectId.contains("bengali", ignoreCase = true)
                "english" -> q.subjectId.contains("english", ignoreCase = true)
                "history" -> q.subjectId.contains("history", ignoreCase = true)
                "geography" -> q.subjectId.contains("geography", ignoreCase = true)
                "life_science" -> q.subjectId.contains("life_sci", ignoreCase = true) || q.subjectId.contains("bio", ignoreCase = true) || q.subjectId.contains("env", ignoreCase = true)
                "physical_science" -> q.subjectId.contains("phy_sci", ignoreCase = true) || q.subjectId.contains("physical", ignoreCase = true)
                "mathematics", "math" -> q.subjectId.contains("math", ignoreCase = true)
                "mega_grand" -> true
                else -> q.subjectId.contains(arenaId, ignoreCase = true)
            }
        }

        val convertedFromDb = matchingEntities.mapNotNull { entity ->
            val rawOptions = if (entity.optionsJson.contains("|")) {
                entity.optionsJson.split("|").map { it.trim() }.filter { it.isNotBlank() }
            } else if (entity.optionsJson.contains(",")) {
                entity.optionsJson.split(",").map { it.trim().removeSurrounding("\"").removeSurrounding("[").removeSurrounding("]") }.filter { it.isNotBlank() }
            } else {
                emptyList()
            }
            if (rawOptions.size >= 2) {
                val safeIdx = entity.correctOptionIndex.coerceIn(0, rawOptions.size - 1)
                LiveQuizItem(
                    id = entity.id,
                    questionBengali = entity.question,
                    questionEnglish = entity.question,
                    optionsBengali = rawOptions,
                    optionsEnglish = rawOptions,
                    correctIndex = safeIdx,
                    explanationBengali = entity.explanation.ifBlank { "সঠিক উত্তর: ${rawOptions[safeIdx]}" },
                    explanationEnglish = entity.explanation.ifBlank { "Correct Answer: ${rawOptions[safeIdx]}" }
                )
            } else null
        }

        // Combine total questions from database with curated high-yield questions, avoiding duplicates
        val totalSubjectPool = (convertedFromDb + curatedFallback).distinctBy { it.questionBengali.trim().lowercase() }
        return if (totalSubjectPool.isNotEmpty()) totalSubjectPool else curatedFallback
    }

    // Active questions - drawn randomly from the TOTAL subject question bank!
    var activeQuestions by remember(selectedArena, dbQuestions) {
        val arena = selectedArena ?: arenas[0]
        val fullPool = buildSubjectPool(arena.id, arena.questions, dbQuestions)
        mutableStateOf(fullPool.shuffled().take(10))
    }

    // Pre-configured top competitors per subject arena with authentic student names and districts
    fun getDefaultTopCompetitors(arenaId: String): List<LiveLeaderboardUser> {
        return when (arenaId) {
            "english" -> listOf(
                LiveLeaderboardUser("তানিয়া সরকার", "Kolkata", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("সৌম্যদীপ ব্যানার্জি", "Hooghly", 100, Color(0xFF10B981)),
                LiveLeaderboardUser("শুভ্রা পাল", "Siliguri", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("রাহুল সেনগুপ্ত", "Howrah", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("অঙ্কিতা মণ্ডল", "North 24 Pgs", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("রোহিত বিশ্বাস", "Nadia", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("প্রিয়া ভৌমিক", "Burdwan", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("দেবজিৎ চক্রবর্তী", "Bankura", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("সুস্মিতা পাল", "Medinipur", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("স্নেহা রায়", "Malda", 60, Color(0xFFF97316))
            )
            "bengali" -> listOf(
                LiveLeaderboardUser("অনিন্দিতা দাস", "Kolkata", 100, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("শৌভিক ভট্টাচার্য", "Howrah", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("অপরাজিতা দত্ত", "Hooghly", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("সায়ন মুখার্জি", "North 24 Pgs", 90, Color(0xFF10B981)),
                LiveLeaderboardUser("রূপসা সেন", "Siliguri", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("সন্দীপন ঘোষ", "Burdwan", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("শ্রেয়া চ্যাটার্জি", "South 24 Pgs", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("অর্কপ্রভ রায়", "Nadia", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("পায়েল সাহা", "Murshidabad", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("অনির্বাণ কর", "Birbhum", 60, Color(0xFFF97316))
            )
            "history" -> listOf(
                LiveLeaderboardUser("অদিতি মজুমদার", "Kolkata", 100, Color(0xFFF59E0B)),
                LiveLeaderboardUser("দেবমাল্য রায়", "Burdwan", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("সুস্মিতা দে", "Medinipur", 90, Color(0xFF10B981)),
                LiveLeaderboardUser("কৌশিক মণ্ডল", "Bankura", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("তৃষা চক্রবর্তী", "Hooghly", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("আকাশ দাস", "Howrah", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("বর্ণালী রায়", "Malda", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("সৌরভ সেন", "Murshidabad", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("মৌসুমী পাল", "Purulia", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("বিপ্লব সরকার", "Jalpaiguri", 60, Color(0xFFF97316))
            )
            "geography" -> listOf(
                LiveLeaderboardUser("স্বাগতা ব্যানার্জি", "Siliguri", 100, Color(0xFF06B6D4)),
                LiveLeaderboardUser("অভিরূপ দত্ত", "Kolkata", 100, Color(0xFF10B981)),
                LiveLeaderboardUser("পাপিয়া রায়", "Darjeeling", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("শুভঙ্কর পাল", "Howrah", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("মানসী ঘোষ", "Nadia", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("রণিত সেন", "North 24 Pgs", 80, Color(0xFF2563EB)),
                LiveLeaderboardUser("ঝিলিক চক্রবর্তী", "Hooghly", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("প্রীতম সাহা", "Cooch Behar", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("সোমা দাস", "Alipurduar", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("ইন্দ্রনীল বসু", "Medinipur", 60, Color(0xFFF97316))
            )
            "life_science" -> listOf(
                LiveLeaderboardUser("ঐশী মুখার্জি", "Kolkata", 100, Color(0xFF10B981)),
                LiveLeaderboardUser("শৌনক পাল", "Burdwan", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("সঞ্চিতা দে", "Hooghly", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("প্রতীক সেন", "Howrah", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("মেঘনা রায়", "Siliguri", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("আবির ঘোষাল", "Bankura", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("রিয়া সরকার", "Nadia", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("ভাস্কর মণ্ডল", "Medinipur", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("তিতলি দাস", "North 24 Pgs", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("অর্পণ চক্রবর্তী", "Birbhum", 60, Color(0xFFF97316))
            )
            "physical_science" -> listOf(
                LiveLeaderboardUser("সৌরদীপ রায়", "Kolkata", 100, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("অস্মিতা পাল", "Hooghly", 100, Color(0xFF10B981)),
                LiveLeaderboardUser("প্রিয়ম সেনগুপ্ত", "Howrah", 90, Color(0xFF2563EB)),
                LiveLeaderboardUser("দিব্যাংশু দাস", "Burdwan", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("স্বস্তিকা সাহা", "Siliguri", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("কৌশল দেব", "North 24 Pgs", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("শ্রুতি চক্রবর্তী", "Nadia", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("অরিন্দম মাইতি", "Medinipur", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("পল্লবী বিশ্বাস", "Murshidabad", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("সুদীপ্ত দে", "Bankura", 60, Color(0xFFF97316))
            )
            "mathematics", "math" -> listOf(
                LiveLeaderboardUser("সপ্তর্ষি দত্ত", "Kolkata", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("অনন্যা মজুমদার", "Howrah", 100, Color(0xFF10B981)),
                LiveLeaderboardUser("দেবায়ুধ পাল", "Hooghly", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("স্মৃতি রায়", "Siliguri", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("রণজয় সেন", "Burdwan", 90, Color(0xFFEC4899)),
                LiveLeaderboardUser("মৌমিতা ঘোষ", "Nadia", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("অর্ক সেনগুপ্ত", "North 24 Pgs", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("তানিশা মণ্ডল", "Medinipur", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("অভিজিৎ সাহা", "Malda", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("শর্মী চ্যাটার্জি", "Purulia", 60, Color(0xFFF97316))
            )
            else -> listOf(
                LiveLeaderboardUser("অঙ্কিতা মণ্ডল", "Kolkata", 100, Color(0xFFE11D48)),
                LiveLeaderboardUser("রাহুল সেনগুপ্ত", "Howrah", 100, Color(0xFF2563EB)),
                LiveLeaderboardUser("প্রিয়া ভৌমিক", "Siliguri", 90, Color(0xFF10B981)),
                LiveLeaderboardUser("শুভম সরকার", "Burdwan", 90, Color(0xFFF59E0B)),
                LiveLeaderboardUser("সুস্মিতা পাল", "Medinipur", 90, Color(0xFF8B5CF6)),
                LiveLeaderboardUser("রোহিত বিশ্বাস", "Nadia", 80, Color(0xFF06B6D4)),
                LiveLeaderboardUser("দেবজিৎ চক্রবর্তী", "Hooghly", 80, Color(0xFF14B8A6)),
                LiveLeaderboardUser("তানিয়া সরকার", "North 24 Pgs", 70, Color(0xFF6366F1)),
                LiveLeaderboardUser("স্নেহা রায়", "Malda", 70, Color(0xFF84CC16)),
                LiveLeaderboardUser("সৌম্য ব্যানার্জি", "Bankura", 60, Color(0xFFF97316))
            )
        }
    }

    // Stores the user's best score in each subject arena
    val arenaUserScoreMap = remember { mutableStateMapOf<String, Int>() }

    // Generates the Top 10 leaderboard for a subject arena, integrating user score
    fun getArenaTop10(arenaId: String, currentScore: Int? = null): List<LiveLeaderboardUser> {
        val defaultList = getDefaultTopCompetitors(arenaId)
        val scoreToUse = currentScore ?: arenaUserScoreMap[arenaId]
        return if (scoreToUse != null) {
            val displayName = studentProfile?.name?.takeIf { it.isNotBlank() } ?: ("আপনি")
            val userItem = LiveLeaderboardUser(
                name = displayName,
                district = "আপনার স্কোর",
                score = scoreToUse,
                avatarColor = PrimaryIndigo,
                isUser = true
            )
            (defaultList + userItem)
                .sortedWith(
                    compareByDescending<LiveLeaderboardUser> { it.score }
                        .thenBy { !it.isUser }
                )
                .take(10)
        } else {
            defaultList.take(10)
        }
    }

    // Calculates user rank in the subject
    fun getUserRankInArena(arenaId: String, score: Int): Int {
        val defaultList = getDefaultTopCompetitors(arenaId)
        val higherScoresCount = defaultList.count { it.score > score }
        return higherScoresCount + 1
    }

    // Countdown before starting - picks 10 fresh random questions from the ENTIRE subject question bank!
    LaunchedEffect(gameState, selectedArena) {
        if (gameState == "COUNTDOWN") {
            val arena = selectedArena ?: arenas[0]
            val totalPool = buildSubjectPool(arena.id, arena.questions, dbQuestions)
            // Draw 10 randomized questions from the total subject question pool
            activeQuestions = totalPool.shuffled().take(10)
            countdownSec = 3
            while (countdownSec > 0) {
                delay(1000)
                countdownSec--
            }
            currentQuestionIndex = 0
            selectedOptionIndex = null
            isAnswerSubmitted = false
            userScore = 0
            correctCount = 0
            wrongCount = 0
            questionTimer = 60
            gameState = "PLAYING"
        }
    }

    // Per-question 1 MINUTE (60 SECONDS) timer: AUTOMATICALLY ADVANCES TO NEXT QUESTION WHEN TIME IS UP!
    LaunchedEffect(gameState, currentQuestionIndex) {
        if (gameState == "PLAYING") {
            questionTimer = 60
            isAnswerSubmitted = false
            selectedOptionIndex = null
            while (questionTimer > 0 && !isAnswerSubmitted) {
                delay(1000)
                if (!isAnswerSubmitted) {
                    questionTimer--
                }
            }
            // If time is up and user didn't submit
            if (!isAnswerSubmitted && questionTimer <= 0) {
                isAnswerSubmitted = true
                wrongCount++
                // Show correct answer for 1.2 seconds, then automatically advance
                delay(1200)
                if (currentQuestionIndex < activeQuestions.size - 1) {
                    currentQuestionIndex++
                } else {
                    gameState = "RESULT"
                }
            }
        }
    }

    // Handle reward and update arena leaderboard on completion
    LaunchedEffect(gameState) {
        if (gameState == "RESULT" && !hasRewardedBonus) {
            hasRewardedBonus = true
            val arenaId = selectedArena?.id ?: "bengali"
            val prevBest = arenaUserScoreMap[arenaId] ?: 0
            if (userScore > prevBest) {
                arenaUserScoreMap[arenaId] = userScore
            }
            val earnedXp = (correctCount * 10) + 50
            val earnedCoins = (correctCount * 10) + 20
            viewModel?.addRewardBonus(earnedXp, earnedCoins)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (gameState) {
                            "LOBBY" -> "লাইভ এরিনা লবি"
                            "DETAILS" -> selectedArena?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: ("লাইভ কুইজ")
                            else -> selectedArena?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: ("লাইভ কুইজ")
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // LIVE badge strictly on the right side with animated pulsing red dot
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(10.dp)
                            ) {
                                // Pulsing expanding halo ring
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .graphicsLayer {
                                            scaleX = dotScale
                                            scaleY = dotScale
                                            alpha = (1f - (dotScale - 0.8f) / 0.55f).coerceIn(0.1f, 0.7f)
                                        }
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                                // Solid center breathing core dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .graphicsLayer {
                                            alpha = dotAlpha.coerceAtLeast(0.6f)
                                        }
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "লাইভ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (gameState == "DETAILS") {
                                gameState = "LOBBY"
                            } else if (gameState == "PLAYING" || gameState == "RESULT") {
                                gameState = "LOBBY"
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("live_quiz_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Replaced bottom tool with Banner Ad with zero padding/margin as requested
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                AdMobBanner()
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (gameState) {
                "LOBBY" -> {
                    // 🌟 লাইভ এরিনা লবি (Live Arena Lobby Screen) - Zero top, AdMob-safe bottom buffer
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 0.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header Banner
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(PrimaryIndigo, Color(0xFF312E81))
                                            )
                                        )
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.FlashOn,
                                                contentDescription = null,
                                                tint = ExamGold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "লাইভ এরিনা লবি",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            text = if (isBengali) "তোমার প্রিয় বিষয় বেছে নাও এবং সারা বাংলার সহপাঠীদের সাথে ১ মিনিটের লাইভ কুইজ লড়াইয়ে অংশ নাও!"
                                            else "Choose your subject arena and compete in 1-minute question battles!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "⏱️ ১ মিনিট প্রতি প্রশ্ন",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "📝 ১০টি প্রশ্ন",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "🏆 +১০০ পয়েন্ট",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ExamGold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "বিষয়ভিত্তিক লাইভ রুমসমূহ:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Subject Arena List
                        items(arenas) { arena ->
                            val preNumber = subjectPreNumbers[arena.id] ?: arena.onlineCount
                            val liveFluctuation = onlineCountsMap[arena.id] ?: 75
                            val totalOnline = preNumber + liveFluctuation
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedArena = arena
                                        gameState = "DETAILS"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = arena.primaryColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = arena.icon,
                                                contentDescription = null,
                                                tint = arena.primaryColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBengali) arena.titleBengali else arena.titleEnglish,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "১০টি প্রশ্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = "১ মিনিট/প্রশ্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .graphicsLayer {
                                                        alpha = dotAlpha.coerceAtLeast(0.5f)
                                                    }
                                                    .background(Color(0xFFEF4444), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${formatBengaliDigits(totalOnline)} জন লাইভ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = arena.primaryColor
                                            )
                                        }
                                    }

                                    // Distinct gap between content and button as requested
                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Tiny button as requested
                                    Button(
                                        onClick = {
                                            selectedArena = arena
                                            gameState = "DETAILS"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = arena.primaryColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = "প্রবেশ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "DETAILS" -> {
                    // Arena Details & Rules Screen before entering
                    val arena = selectedArena ?: arenas[0]
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Surface(
                                shape = CircleShape,
                                color = arena.primaryColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = arena.icon,
                                        contentDescription = null,
                                        tint = arena.primaryColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isBengali) arena.titleBengali else arena.titleEnglish,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "১০টি গুরুত্বপূর্ণ কমন প্রশ্ন • ১ মিনিট প্রতি প্রশ্ন",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Rules Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "নিয়মাবলী ও পয়েন্ট ব্যবস্থা:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RuleItem(
                                            icon = Icons.Default.Timer,
                                            title = "১ মিনিট",
                                            subtitle = "প্রতি প্রশ্নে ৬০ সেকেন্ড",
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.weight(1f)
                                        )
                                        RuleItem(
                                            icon = Icons.Default.Bolt,
                                            title = "১০ পয়েন্ট",
                                            subtitle = "সঠিক উত্তরে ১০ pt",
                                            color = Color(0xFF3B82F6),
                                            modifier = Modifier.weight(1f)
                                        )
                                        RuleItem(
                                            icon = Icons.Default.EmojiEvents,
                                            title = "১০০ পয়েন্ট",
                                            subtitle = "১০টি প্রশ্নে মোট",
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Start Button
                        item {
                            Button(
                                onClick = { gameState = "COUNTDOWN" },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = arena.primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("live_quiz_start_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "কুইজ শুরু করুন",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Top 10 Live Leaderboard Card as requested!
                        item {
                            val currentTop10 = getArenaTop10(arena.id)
                            val userScoreInThisArena = arenaUserScoreMap[arena.id]
                            val userRank = userScoreInThisArena?.let { getUserRankInArena(arena.id, it) }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = ExamGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "শীর্ষ ১০ লিডারবোর্ড",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = arena.primaryColor.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "সঠিক উত্তর = ১০ pt",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = arena.primaryColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    // Display 10 players
                                    currentTop10.forEachIndexed { index, userItem ->
                                        val rank = index + 1
                                        val isCurrentUser = userItem.isUser

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrentUser) PrimaryIndigo.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isCurrentUser) BorderStroke(1.5.dp, PrimaryIndigo) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rank Badge
                                                Box(
                                                    modifier = Modifier.width(32.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    when (rank) {
                                                        1 -> Text("🥇", fontSize = 18.sp)
                                                        2 -> Text("🥈", fontSize = 18.sp)
                                                        3 -> Text("🥉", fontSize = 18.sp)
                                                        else -> Text(
                                                            text = "#${formatBengaliDigits(rank)}",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Student Avatar Initial
                                                Surface(
                                                    shape = CircleShape,
                                                    color = userItem.avatarColor.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = userItem.name.firstOrNull()?.toString() ?: "U",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = userItem.avatarColor
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // Student Name and District
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = userItem.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold
                                                            ),
                                                            color = if (isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1
                                                        )
                                                        if (isCurrentUser) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "(আপনি)",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = PrimaryIndigo
                                                            )
                                                        }
                                                    }
                                                    if (userItem.district.isNotBlank()) {
                                                        Text(
                                                            text = userItem.district,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Total Points badge
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (rank <= 3) ExamGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (rank <= 3) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = "${formatBengaliDigits(userItem.score)} pt",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (rank <= 3) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // If user has played and is ranked outside top 10, show user's score row below
                                    if (userScoreInThisArena != null && userRank != null && userRank > 10 && currentTop10.none { it.isUser }) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.14f),
                                            border = BorderStroke(1.5.dp, PrimaryIndigo),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${formatBengaliDigits(userRank)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.width(32.dp)
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = PrimaryIndigo.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "⭐",
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${studentProfile?.name ?: "আপনার স্কোর"} (আপনি)",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = PrimaryIndigo.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, PrimaryIndigo)
                                                ) {
                                                    Text(
                                                        text = "${formatBengaliDigits(userScoreInThisArena)} pt",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = PrimaryIndigo,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "COUNTDOWN" -> {
                    // Pulsing Countdown Screen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryIndigo.copy(alpha = 0.15f),
                                modifier = Modifier.size(140.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$countdownSec",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 64.sp
                                        ),
                                        color = PrimaryIndigo
                                    )
                                }
                            }

                            Text(
                                text = "লাইভ কুইজ প্রস্তুত হচ্ছে...",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )

                            Text(
                                text = "প্রতি প্রশ্নে ১ মিনিট (৬০ সেকেন্ড) সময় পাবেন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                "PLAYING" -> {
                    // Active Live Quiz Game Screen
                    val currentQuestion = activeQuestions[currentQuestionIndex]
                    val questionText = if (isBengali) currentQuestion.questionBengali else currentQuestion.questionEnglish
                    val options = if (isBengali) currentQuestion.optionsBengali else currentQuestion.optionsEnglish
                    val totalQuestions = activeQuestions.size

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with question number, timer and live score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryIndigo.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "প্রশ্ন ${currentQuestionIndex + 1} / $totalQuestions",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Live Timer with alert color (60s countdown)
                            val timerColor = when {
                                questionTimer <= 10 -> Color(0xFFEF4444)
                                questionTimer <= 20 -> Color(0xFFF59E0B)
                                else -> PrimaryIndigo
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = timerColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, timerColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = timerColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${questionTimer}s",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = timerColor
                                    )
                                }
                            }

                            // Current live score
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExamGold.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$userScore pt",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // Progress bar for entire quiz
                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1) / totalQuestions.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PrimaryIndigo,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        // Question Card: BORDER RADIUS VERY LOW (4.dp) and FONT SIZE 19dp (19.sp)
                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = questionText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        lineHeight = 26.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Options List (Clickable with distinctive background colors for option numbers!)
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(options) { index, optionText ->
                                val isSelected = selectedOptionIndex == index
                                val isCorrect = index == currentQuestion.correctIndex

                                val optionBorderColor = when {
                                    isAnswerSubmitted && isCorrect -> CorrectGreen
                                    isAnswerSubmitted && isSelected && !isCorrect -> WrongRed
                                    isSelected -> PrimaryIndigo
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                }

                                val optionContainerColor = when {
                                    isAnswerSubmitted && isCorrect -> CorrectGreen.copy(alpha = 0.15f)
                                    isAnswerSubmitted && isSelected && !isCorrect -> WrongRed.copy(alpha = 0.15f)
                                    isSelected -> PrimaryIndigo.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surface
                                }

                                // Distinctive vibrant colors for option numbers (ক, খ, গ, ঘ)
                                val defaultBadgeColor = when (index) {
                                    0 -> Color(0xFF2563EB) // Royal Blue
                                    1 -> Color(0xFF059669) // Vibrant Emerald
                                    2 -> Color(0xFFD97706) // Vibrant Amber/Orange
                                    else -> Color(0xFF7C3AED) // Vibrant Purple
                                }

                                val badgeBgColor = when {
                                    isAnswerSubmitted && isCorrect -> CorrectGreen
                                    isAnswerSubmitted && isSelected && !isCorrect -> WrongRed
                                    isSelected -> PrimaryIndigo
                                    else -> defaultBadgeColor
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = optionContainerColor),
                                    border = BorderStroke(if (isSelected || (isAnswerSubmitted && isCorrect)) 2.dp else 1.dp, optionBorderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("live_quiz_option_$index")
                                        .clickable(enabled = !isAnswerSubmitted) {
                                            // USER CLICKED AN OPTION
                                            selectedOptionIndex = index
                                            isAnswerSubmitted = true

                                            if (index == currentQuestion.correctIndex) {
                                                // Correct answer: 10 pt per correct answer as requested!
                                                userScore += 10
                                                correctCount++
                                            } else {
                                                wrongCount++
                                            }

                                            // AUTOMATICALLY MOVE TO NEXT QUESTION AFTER 1.2s!
                                            coroutineScope.launch {
                                                delay(1200)
                                                if (currentQuestionIndex < totalQuestions - 1) {
                                                    currentQuestionIndex++
                                                    selectedOptionIndex = null
                                                    isAnswerSubmitted = false
                                                } else {
                                                    gameState = "RESULT"
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Option Letter Badge with vibrant background color
                                        Surface(
                                            shape = CircleShape,
                                            color = badgeBgColor,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                val letter = when (index) {
                                                    0 -> "ক"
                                                    1 -> "খ"
                                                    2 -> "গ"
                                                    else -> "ঘ"
                                                }
                                                Text(
                                                    text = letter,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected || (isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Status icon if submitted
                                        if (isAnswerSubmitted) {
                                            if (isCorrect) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CorrectGreen,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            } else if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Cancel,
                                                    contentDescription = null,
                                                    tint = WrongRed,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Show explanation if answered
                            if (isAnswerSubmitted) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "ব্যাখ্যা:",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = PrimaryIndigo
                                            )
                                            Text(
                                                text = if (isBengali) currentQuestion.explanationBengali else currentQuestion.explanationEnglish,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "RESULT" -> {
                    // Final Contest Results & Leaderboard screen
                    val total = activeQuestions.size
                    val percentage = (correctCount * 100) / total
                    val userRank = when {
                        percentage >= 80 -> 3
                        percentage >= 60 -> 11
                        percentage >= 40 -> 24
                        else -> 48
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Celebration Badge
                        item {
                            Surface(
                                shape = CircleShape,
                                color = ExamGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(90.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = ExamGold,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "প্রতিযোগিতা সম্পন্ন!",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "তুমি এই এরিনায় ${userRank}তম স্থান অর্জন করেছো!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Score & Reward Summary Card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        ResultStatItem(
                                            title = "মোট পয়েন্ট",
                                            value = "$userScore",
                                            color = PrimaryIndigo
                                        )
                                        ResultStatItem(
                                            title = "সঠিক উত্তর",
                                            value = "$correctCount / $total",
                                            color = CorrectGreen
                                        )
                                        ResultStatItem(
                                            title = "অর্জিত পুরষ্কার",
                                            value = "+${(correctCount * 25) + 100} XP",
                                            color = ExamGold
                                        )
                                    }
                                }
                            }
                        }

                        // Podium / Leaderboard Table
                        item {
                            val arenaId = selectedArena?.id ?: "bengali"
                            val resultTop10 = getArenaTop10(arenaId, userScore)
                            val finalUserRank = getUserRankInArena(arenaId, userScore)

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "শীর্ষ ১০ লিডারবোর্ড:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ExamGold.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "তোমার র‍্যাংক: #${formatBengaliDigits(finalUserRank)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    resultTop10.forEachIndexed { idx, comp ->
                                        val rank = idx + 1
                                        val isCurrentUser = comp.isUser
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrentUser) PrimaryIndigo.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            border = if (isCurrentUser) BorderStroke(1.5.dp, PrimaryIndigo) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier.width(32.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    when (rank) {
                                                        1 -> Text("🥇", fontSize = 18.sp)
                                                        2 -> Text("🥈", fontSize = 18.sp)
                                                        3 -> Text("🥉", fontSize = 18.sp)
                                                        else -> Text(
                                                            text = "#${formatBengaliDigits(rank)}",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (isCurrentUser) "${comp.name} (আপনি) ⭐" else comp.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${formatBengaliDigits(comp.score)} pt",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (rank <= 3) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    if (finalUserRank > 10 && resultTop10.none { it.isUser }) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.14f),
                                            border = BorderStroke(1.5.dp, PrimaryIndigo),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${formatBengaliDigits(finalUserRank)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.width(32.dp)
                                                )
                                                Text(
                                                    text = "${studentProfile?.name ?: "তুমি"} (আপনি) ⭐",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${formatBengaliDigits(userScore)} pt",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action Buttons: Play Again & Back to Lobby
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        gameState = "COUNTDOWN"
                                        hasRewardedBonus = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("live_quiz_play_again_btn")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "এই বিষয়টি আবার খেলুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        gameState = "LOBBY"
                                        hasRewardedBonus = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("live_quiz_other_subject_btn")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "অন্য বিষয় নির্বাচন করুন (লবি)",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ResultStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
