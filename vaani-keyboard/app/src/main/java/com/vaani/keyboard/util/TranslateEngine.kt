package com.vaani.keyboard.util

object TranslateEngine {

    fun translate(input: String): String {
        if (input.isBlank()) return ""

        val normalized = input.lowercase().trim().replace(Regex("\\s+"), " ")

        val exact = phrasebook[normalized]
        if (exact != null) return GrammarEngine.clean(exact)

        for ((pattern, template) in templates) {
            val match = pattern.matchEntire(normalized)
            if (match != null) {
                val result = template(match)
                return GrammarEngine.clean(result)
            }
        }

        val words = normalized.split(" ")
        var translated = words.joinToString(" ") { word ->
            wordDictionary[word] ?: word
        }

        translated = reorderToEnglish(translated)

        return GrammarEngine.clean(translated)
    }

    private val phrasebook: Map<String, String> = buildMap {
        // ─── Greetings & Pleasantries ───
        put("namaste", "Hello")
        put("namaskar", "Hello")
        put("namaste doston", "Hello friends")
        put("namaste sabko", "Hello everyone")
        put("sabko namaste", "Hello everyone")
        put("hello", "Hello")
        put("hi", "Hi")
        put("hey", "Hey")
        put("kaise ho", "How are you")
        put("kaise ho aap", "How are you")
        put("aap kaise hain", "How are you")
        put("tum kaise ho", "How are you")
        put("kya haal hai", "How are you")
        put("kya haal hain", "How are you")
        put("kya haal", "What's up")
        put("sab theek", "Everything is fine")
        put("sab theek hai", "Everything is fine")
        put("sab achha hai", "Everything is good")
        put("sab badhiya hai", "Everything is great")
        put("main theek hoon", "I am fine")
        put("main theek hun", "I am fine")
        put("main achha hoon", "I am good")
        put("mai theek hoon", "I am fine")
        put("mast hoon", "I am great")
        put("badhiya hoon", "I am doing great")
        put("thoda thoda", "So so")
        put("achha laga aapko dekhkar", "Nice to see you")
        put("aapse milkar achha laga", "Nice to meet you")
        put("mujhe bhi achha laga", "Nice to meet you too")
        put("milte hain", "See you")
        put("phir milenge", "See you later")
        put("phir milte hain", "See you later")
        put("kal milte hain", "See you tomorrow")
        put("jaldi milte hain", "See you soon")
        put("bye", "Bye")
        put("goodbye", "Goodbye")
        put("achha chalta hoon", "Okay, I am leaving")

        // ─── Thank you & Politeness ───
        put("dhanyavaad", "Thank you")
        put("shukriya", "Thank you")
        put("thank you", "Thank you")
        put("thanks", "Thanks")
        put("bahut dhanyavaad", "Thank you very much")
        put("bahut shukriya", "Thank you very much")
        put("thank you so much", "Thank you so much")
        put("aapka dhanyavaad", "Thank you")
        put("meri taraf se dhanyavaad", "Thank you on my behalf")
        put("kripya", "Please")
        put("please", "Please")
        put("maaf karna", "Sorry")
        put("maaf kijiye", "I am sorry")
        put("sorry", "Sorry")
        put("i am sorry", "I am sorry")
        put("koi baat nahi", "No problem")
        put("koi baat nahi hai", "It is no problem")
        put("koi nahi", "Never mind")
        put("chinta mat karo", "Don't worry")
        put("chinta na karo", "Don't worry")
        put("fikar mat karo", "Don't worry")
        put("tension mat lo", "Don't stress")
        put("no problem", "No problem")
        put("its okay", "It is okay")
        put("theek hai", "Okay")
        put("thik hai", "Okay")
        put("achha", "Okay")
        put("okay", "Okay")
        put("ok", "Okay")
        put("haan", "Yes")
        put("haan ji", "Yes")
        put("haan bilkul", "Yes definitely")
        put("bilkul", "Definitely")
        put("ji haan", "Yes")
        put("nahi", "No")
        put("nahi ji", "No")
        put("nahi bilkul nahi", "No not at all")
        put("bilkul nahi", "Not at all")

        // ─── Questions ───
        put("kya kar rahe ho", "What are you doing")
        put("kya kar rahe hain", "What are you doing")
        put("kya kar raha hai", "What is he doing")
        put("kya kar rahi hai", "What is she doing")
        put("aap kya kar rahe hain", "What are you doing")
        put("kya ho raha hai", "What is happening")
        put("kya chal raha hai", "What is going on")
        put("kya hua", "What happened")
        put("kya hua hai", "What has happened")
        put("aap kahan hain", "Where are you")
        put("tum kahan ho", "Where are you")
        put("kahan ho", "Where are you")
        put("aap kahan rehte hain", "Where do you live")
        put("tum kahan rehte ho", "Where do you live")
        put("kahan rehte ho", "Where do you live")
        put("aap kab aa rahe hain", "When are you coming")
        put("tum kab aa rahe ho", "When are you coming")
        put("kab aa rahe ho", "When are you coming")
        put("kab aaoge", "When will you come")
        put("kab aaogi", "When will you come")
        put("kab aayega", "When will it come")
        put("aap kaun hain", "Who are you")
        put("ye kaun hai", "Who is this")
        put("wahan kaun hai", "Who is there")
        put("aap kyon puchh rahe hain", "Why are you asking")
        put("kyon", "Why")
        put("kab", "When")
        put("kaise pata", "How do you know")
        put("aapko kaise pata", "How do you know")
        put("tumhe kaise pata", "How do you know")
        put("kitna hua", "How much is it")
        put("ye kitna hai", "How much is this")
        put("ye kya hai", "What is this")
        put("woh kya hai", "What is that")
        put("aap kya keh rahe hain", "What are you saying")
        put("aap kya bol rahe hain", "What are you saying")
        put("kya matlab hai", "What does it mean")
        put("iska matlab kya hai", "What does this mean")
        put("samajh mein nahi aaya", "I did not understand")
        put("samjha nahi", "Did not understand")
        put("samajh aaya", "Understood")
        put("samajh gaya", "Understood")
        put("samajh gayi", "Understood")
        put("achha samajh gaya", "Okay understood")

        // ─── Time ───
        put("aaj", "Today")
        put("aaj kal", "Nowadays")
        put("kal", "Tomorrow / Yesterday")
        put("parson", "Day after tomorrow / Day before yesterday")
        put("aaj raat", "Tonight")
        put("aaj subah", "This morning")
        put("aaj shaam", "This evening")
        put("subah", "Morning")
        put("shaam", "Evening")
        put("raat", "Night")
        put("dopahar", "Afternoon")
        put("abhi", "Right now")
        put("abhi abhi", "Just now")
        put("thodi der mein", "In a little while")
        put("kuch der mein", "In a while")
        put("ek minute", "One minute")
        put("do minute", "Two minutes")
        put("thoda ruko", "Wait a moment")
        put("thodi der ruko", "Wait a little")
        put("zara ruko", "Wait a bit")
        put("thodi der baad", "After some time")
        put("kuch der baad", "After a while")
        put("jaldi karo", "Hurry up")
        put("jaldi karo na", "Please hurry up")
        put("jaldi", "Quickly")
        put("der ho gayi", "It got late")
        put("der ho rahi hai", "It is getting late")
        put("time nahi hai", "There is no time")
        put("waqt nahi hai", "There is no time")
        put("abhi busy hoon", "I am busy right now")
        put("abhi kaam kar raha hoon", "I am working right now")
        put("so jaao", "Go to sleep")
        put("so gaya", "Went to sleep")
        put("so gayi", "Went to sleep")
        put("uth jaao", "Wake up")
        put("jag jaao", "Wake up")
        put("neend aa rahi hai", "I am feeling sleepy")
        put("neend aa gayi", "I feel sleepy")
        put("thak gaya hoon", "I am tired")
        put("thak gayi hoon", "I am tired")

        // ─── Family ───
        put("mummy", "Mom")
        put("maa", "Mom")
        put("mata ji", "Mother")
        put("papa", "Dad")
        put("pita ji", "Father")
        put("bhai", "Brother")
        put("bhaiya", "Brother")
        put("chota bhai", "Little brother")
        put("bada bhai", "Elder brother")
        put("behen", "Sister")
        put("didi", "Sister")
        put("choti behen", "Little sister")
        put("badi behen", "Elder sister")
        put("pati", "Husband")
        put("patni", "Wife")
        put("biwi", "Wife")
        put("beta", "Son")
        put("beti", "Daughter")
        put("dada", "Grandfather")
        put("dadi", "Grandmother")
        put("nana", "Grandfather (maternal)")
        put("nani", "Grandmother (maternal)")
        put("chacha", "Uncle")
        put("chachi", "Aunt")
        put("mama", "Uncle (maternal)")
        put("mami", "Aunt (maternal)")
        put("tau", "Uncle (elder)")
        put("tai", "Aunt (elder)")
        put("bua", "Aunt (father's sister)")
        put("fufa", "Uncle (father's sister's husband)")
        put("bhabhi", "Sister-in-law")
        put("jija", "Brother-in-law")
        put("saas", "Mother-in-law")
        put("sasur", "Father-in-law")
        put("nana ji", "Grandfather")
        put("nani ji", "Grandmother")
        put("rishtedar", "Relatives")
        put("ghar wale", "Family members")

        // ─── Food ───
        put("khana kha liya", "Had my meal")
        put("khana kha liya hai", "I have eaten")
        put("khana kha lo", "Please eat")
        put("khana khao", "Eat your food")
        put("khana banao", "Cook food")
        put("khana ban gaya", "Food is ready")
        put("khana achha hai", "The food is good")
        put("khana bahut achha hai", "The food is very good")
        put("kya khana hai", "What should we eat")
        put("kya bana hai khane mein", "What is for food")
        put("aaj khane mein kya hai", "What is for food today")
        put("pet bhar gaya", "I am full")
        put("bhookh lagi hai", "I am hungry")
        put("bhookh lag gayi", "I feel hungry")
        put("pani peelo", "Drink water")
        put("pani lao", "Bring water")
        put("chai piyo", "Drink tea")
        put("chai banao", "Make tea")
        put("chai ban gayi", "Tea is ready")
        put("chai pe li", "Had tea")
        put("chai pio", "Have tea")
        put("nasta karo", "Have snacks")
        put("nasta ho gaya", "Finished snacks")
        put("roti kha lo", "Eat the bread")
        put("sabzi achhi hai", "The vegetable is good")
        put("kya pakaya hai aaj", "What did you cook today")
        put("maza aa gaya", "It was delicious")
        put("bahut tasty hai", "It is very tasty")
        put("achha lag raha hai", "It feels good")
        put("garam garam", "Hot and fresh")
        put("thanda ho gaya", "It became cold")

        // ─── Travel & Location ───
        put("ghar aa raha hoon", "I am coming home")
        put("ghar aa gaye", "Reached home")
        put("ghar aa gaya", "Reached home")
        put("ghar aa gayi", "Reached home")
        put("ghar pahunch gaya", "Reached home")
        put("ghar pahunch gayi", "Reached home")
        put("ghar pahunch gaye", "Reached home")
        put("office ja raha hoon", "I am going to the office")
        put("office ja rahi hoon", "I am going to the office")
        put("school ja raha hoon", "I am going to school")
        put("college ja raha hoon", "I am going to college")
        put("bazaar ja raha hoon", "I am going to the market")
        put("bazaar gaya tha", "Went to the market")
        put("bahar gaya hoon", "I am outside")
        put("bahar hoon", "I am out")
        put("rasta bhool gaya", "Lost the way")
        put("rasta nahi mil raha", "Can't find the way")
        put("yahan aao", "Come here")
        put("idhar aao", "Come here")
        put("wahan jao", "Go there")
        put("udhar jao", "Go there")
        put("pass mein hai", "It is nearby")
        put("dur hai", "It is far")
        put("kitna dur hai", "How far is it")
        put("kitna samay lagta hai", "How long does it take")
        put("time lagta hai", "It takes time")
        put("nikal gaya", "Left / Departed")
        put("nikal gayi", "Left / Departed")
        put("nikal gaye", "Left / Departed")
        put("chalte hain", "Let us go")
        put("chalo", "Let us go")
        put("chalo chalein", "Let us go")
        put("niklo", "Start moving / Leave")
        put("nikalte hain", "Let us leave")
        put("nikal raha hoon", "I am leaving")
        put("nikal rahi hoon", "I am leaving")
        put("aa raha hoon", "I am coming")
        put("aa rahi hoon", "I am coming")
        put("aap kahan milege", "Where should we meet")

        // ─── Work & Study ───
        put("kaam kar raha hoon", "I am working")
        put("kaam kar rahi hoon", "I am working")
        put("kaam khatam ho gaya", "Work is finished")
        put("kaam ho gaya", "Work is done")
        put("homework kar liya", "Finished homework")
        put("homework nahi hua", "Could not do homework")
        put("padhai kar raha hoon", "I am studying")
        put("padhai kar rahi hoon", "I am studying")
        put("padhai ho gayi", "Studies are done")
        put("exam hai", "There is an exam")
        put("exam mein", "In the exam")
        put("exam khatam ho gaya", "The exam is over")
        put("paper achha gaya", "The paper went well")
        put("paper achha nahi gaya", "The paper did not go well")
        put("result aa gaya", "The result came out")
        put("naukri lag gayi", "Got the job")
        put("job lag gayi", "Got the job")
        put("interview hai", "There is an interview")
        put("meeting hai", "There is a meeting")
        put("meeting mein hoon", "I am in a meeting")
        put("presentation deni hai", "I have to give a presentation")
        put("project submit karna hai", "I have to submit the project")
        put("assignment khatam karna hai", "I have to finish the assignment")
        put("deadline hai", "There is a deadline")
        put("aaj chhutti hai", "Today is a holiday")
        put("kal chhutti hai", "Tomorrow is a holiday")
        put("office se ghar aa raha hoon", "I am coming home from office")

        // ─── Feelings & Emotions ───
        put("khush hoon", "I am happy")
        put("bahut khush hoon", "I am very happy")
        put("khushi ho rahi hai", "I am feeling happy")
        put("dukh hua", "That hurt")
        put("bahut dukh hua", "It is very sad")
        put("udaas hoon", "I am sad")
        put("tension hai", "I am stressed")
        put("bahut tension hai", "I am very stressed")
        put("ghabrahat ho rahi hai", "I am feeling nervous")
        put("dar lag raha hai", "I am scared")
        put("dar lagta hai", "I feel scared")
        put("gussa a raha hai", "I am getting angry")
        put("gussa ho", "Are you angry")
        put("gussa mat ho", "Don't get angry")
        put("bore ho raha hoon", "I am feeling bored")
        put("bore ho gaye", "Got bored")
        put("maza aa raha hai", "I am having fun")
        put("maza nahi aa raha", "I am not having fun")
        put("achha laga", "Felt good")
        put("achha nahi laga", "Did not feel good")
        put("bahut achha laga", "Felt very good")
        put("yaad aa rahe ho", "I am missing you")
        put("yaad aa rahi hai", "I am missing")
        put("bahut yaad aa rahe ho", "I miss you a lot")
        put("miss kar raha hoon", "I am missing you")
        put("pyaar karta hoon", "I love you")
        put("pyaar karti hoon", "I love you")
        put("i love you", "I love you")
        put("main tumse pyaar karta hoon", "I love you")
        put("main aapse pyaar karti hoon", "I love you")
        put("pyaar hai", "There is love")
        put("care karta hoon", "I care")
        put("care karti hoon", "I care")

        // ─── Requests & Commands ───
        put("mujhe batao", "Tell me")
        put("mujhe batayein", "Tell me")
        put("batao na", "Please tell me")
        put("batao", "Tell me")
        put("batana", "Please tell")
        put("mujhe bolo", "Tell me")
        put("mujhe batao na", "Please tell me")
        put("mujhe dikhao", "Show me")
        put("mujhe de do", "Give it to me")
        put("mujhe de do na", "Please give it to me")
        put("ye lo", "Here you go")
        put("le lo", "Take it")
        put("aap le lo", "You take it")
        put("rakh lo", "Keep it")
        put("mujhe chahiye", "I need it")
        put("mujhe nahi chahiye", "I do not need it")
        put("ye karo", "Do this")
        put("ye mat karo", "Do not do this")
        put("aisa mat karo", "Do not do like this")
        put("mujhe madad chahiye", "I need help")
        put("mujhe help chahiye", "I need help")
        put("help karo", "Help")
        put("madad karo", "Help")
        put("mujhe bacha lo", "Save me")
        put("sambhalo", "Take care")
        put("sambhal ke", "Be careful")
        put("dhyaan rakhna", "Take care")
        put("apna khayal rakhna", "Take care of yourself")
        put("apna dhyan rakhna", "Take care of yourself")

        // ─── Health ───
        put("tabiyat theek hai", "Health is fine")
        put("tabiyat kharab hai", "I am not feeling well")
        put("tabiyat achhi nahi hai", "I am not feeling well")
        put("bimaar hoon", "I am sick")
        put("bimaar ho gaye", "I fell sick")
        put("bukhar hai", "I have a fever")
        put("sir dard hai", "I have a headache")
        put("pet dard hai", "I have a stomach ache")
        put("pet mein dard hai", "I have a stomach ache")
        put("khansi hai", "I have a cough")
        put("zukaam hai", "I have a cold")
        put("thand lag gayi", "Got a cold")
        put("daid hai", "I have diarrhea")
        put("chot lag gayi", "I got hurt")
        put("dard ho raha hai", "It hurts")
        put("dard hai", "There is pain")
        put("doctor ke paas jaana hai", "I have to go to the doctor")
        put("dawai le lo", "Take the medicine")
        put("dawai kha lo", "Take the medicine")
        put("dawai de do", "Give the medicine")
        put("dawai khatam ho gayi", "Medicine is finished")
        put("hospital jaana hai", "I have to go to the hospital")
        put("doctor ko dikhana hai", "I have to show the doctor")
        put("operation hua", "There was an operation")
        put("check-up karwana hai", "I need to get a check-up")

        // ─── Shopping & Money ───
        put("kitne ka hai", "How much does it cost")
        put("ye kitne ka hai", "How much is this")
        put("kitne rupaye", "How many rupees")
        put("kitne paise", "How much money")
        put("bahut mehnga hai", "It is very expensive")
        put("mehnga hai", "It is expensive")
        put("sasta hai", "It is cheap")
        put("sasta nahi hai", "It is not cheap")
        put("discount do", "Give a discount")
        put("thoda kam karo", "Reduce the price a bit")
        put("paisa de do", "Give the money")
        put("paisa le lo", "Take the money")
        put("paisa nahi hai", "There is no money")
        put("paise nahi hai", "There is no money")
        put("bank jaana hai", "I have to go to the bank")
        put("ATM jaana hai", "I have to go to the ATM")
        put("bill bharna hai", "I have to pay the bill")
        put("bill kitna hai", "How much is the bill")
        put("kharidna hai", "I want to buy")
        put("leni hai", "I want to take it")
        put("dena hai", "I have to give")
        put("le aana", "Bring it")
        put("le jaana", "Take it away")
        put("lao", "Bring")
        put("kahan se milta hai", "Where can I get it")
        put("kahan milega", "Where will I get it")

        // ─── Phone & Tech ───
        put("phone karo", "Call")
        put("phone kar do", "Make a call")
        put("call karo", "Call")
        put("call kar do", "Make a call")
        put("missed call do", "Give a missed call")
        put("message bhejo", "Send a message")
        put("message bhej do", "Send a message")
        put("message aaya", "Got a message")
        put("message padh lo", "Read the message")
        put("watsapp karo", "WhatsApp me")
        put("photo bhejo", "Send the photo")
        put("photo bhej do", "Send the photo")
        put("image bhejo", "Send the image")
        put("video bhejo", "Send the video")
        put("file bhejo", "Send the file")
        put("download karo", "Download it")
        put("upload karo", "Upload it")
        put("internet nahi chal raha", "The internet is not working")
        put("network nahi hai", "There is no network")
        put("phone kaam nahi kar raha", "The phone is not working")
        put("battery khatam ho gayi", "The battery died")
        put("battery low hai", "The battery is low")
        put("charge karo", "Charge it")
        put("charger nahi hai", "There is no charger")
        put("wifi nahi chal raha", "The WiFi is not working")
        put("password kya hai", "What is the password")
        put("number do", "Give me the number")
        put("number bhejo", "Send the number")
        put("address bhejo", "Send the address")
        put("location bhejo", "Send the location")

        // ─── Weather & Nature ───
        put("mausam achha hai", "The weather is good")
        put("mausam kharab hai", "The weather is bad")
        put("aaj mausam achha hai", "The weather is good today")
        put("barish ho rahi hai", "It is raining")
        put("barish aa gayi", "It started raining")
        put("moosladhar barish", "Heavy rain")
        put("dhoop nikli", "The sun came out")
        put("dhoop bahut hai", "It is very sunny")
        put("garmi hai", "It is hot")
        put("bahut garmi hai", "It is very hot")
        put("thand hai", "It is cold")
        put("bahut thand hai", "It is very cold")
        put("sardi hai", "It is cold")
        put(" hawa chal rahi hai", "The wind is blowing")
        put("andhera ho gaya", "It became dark")
        put("aaj andhera hai", "It is dark today")
        put("badal chhaye hain", "The sky is cloudy")
        put("bijli chamak rahi hai", "Lightning is flashing")
        put("garaj sunai de rahi hai", "Thunder can be heard")
        put("kohra hai", "There is fog")

        // ─── Celebrations ───
        put("happy diwali", "Happy Diwali")
        put("diwali mubarak", "Happy Diwali")
        put("happy holi", "Happy Holi")
        put("holi mubarak", "Happy Holi")
        put("happy dusshera", "Happy Dussehra")
        put("happy raksha bandhan", "Happy Raksha Bandhan")
        put("happy janmashtami", "Happy Janmashtami")
        put("happy ganesh chaturthi", "Happy Ganesh Chaturthi")
        put("happy navratri", "Happy Navratri")
        put("happy eid", "Happy Eid")
        put("eid mubarak", "Happy Eid")
        put("happy christmas", "Happy Christmas")
        put("merry christmas", "Merry Christmas")
        put("happy new year", "Happy New Year")
        put("nav varsh ki shubhkamnaye", "Happy New Year")
        put("happy birthday", "Happy Birthday")
        put("janamdin mubarak ho", "Happy Birthday")
        put("birthday mubarak ho", "Happy Birthday")
        put("janamdin ki badhai", "Happy Birthday")
        put("badhai ho", "Congratulations")
        put("mubarak ho", "Congratulations")
        put("congratulations", "Congratulations")
        put("shubhkamnaye", "Best wishes")
        put("sabko badhai", "Congratulations to everyone")

        // ─── Short Replies ───
        put("achha", "Okay")
        put("achha hai", "That is good")
        put("achha kiya", "Well done")
        put("sahi hai", "That is right")
        put("sahi kaha", "You are right")
        put("sahi pakde ho", "You are correct")
        put("galat hai", "That is wrong")
        put("galat kaha", "You are wrong")
        put("galat ho raha hai", "It is going wrong")
        put("ho gaya", "It is done")
        put("ho gya", "It is done")
        put("nahi hua", "It did not happen")
        put("nahi ho paya", "Could not do it")
        put("pakka", "Definitely")
        put("pakka hai", "Are you sure")
        put("pakka nahi hai", "Not sure")
        put("sach mein", "Really")
        put("sach", "Truth / Really")
        put("jhooth", "Lie")
        put("jhooth mat bolo", "Do not lie")
        put("bohot", "Very / A lot")
        put("bahut", "Very / A lot")
        put("thoda", "A little")
        put("thoda aur", "A little more")
        put("aur", "More / And")
        put("bas", "Enough / That's it")
        put("bas itna", "That is all")
        put("bas ho gaya", "That is enough")
        put("ab bas karo", "Stop now")
        put("ruko", "Stop / Wait")
        put("ruk jaao", "Stop now")
        put("chup raho", "Be quiet")
        put("chup", "Quiet")
        put("sunna", "Listen")
        put("suno", "Listen")
        put("sun lo", "Please listen")
        put("dekho", "Look / See")
        put("dekh lo", "Please see")
        put("dekhna", "Watch / See")
        put("dhyaan do", "Pay attention")
        put("focus karo", "Focus")

        // ─── Instructions & Directions ───
        put("seedha jao", "Go straight")
        put("seedha", "Straight")
        put("dayein mudho", "Turn right")
        put("right le lo", "Take a right")
        put("baayein mudho", "Turn left")
        put("left le lo", "Take a left")
        put("upar jao", "Go up")
        put("neeche jao", "Go down")
        put("andar aao", "Come in")
        put("andar jao", "Go inside")
        put("bahar jao", "Go outside")
        put("yahan ruko", "Stop here")
        put("yahan khade ho jao", "Stand here")
        put("line mein lage", "Stand in line")
        put("piche hato", "Step back")
        put("aage bado", "Move forward")
        put("side mein hato", "Move aside")
        put("raasta do", "Give way")
        put("andar rakho", "Keep inside")
        put("bahar rakho", "Keep outside")
        put("yahan rakho", "Keep here")
        put("wahan rakho", "Keep there")
        put("sambhal ke rakho", "Handle with care")

        // ─── Miscellaneous Common ───
        put("aaja", "Come")
        put("aajao", "Come")
        put("jaa", "Go")
        put("jao", "Go")
        put("aaja na", "Please come")
        put("jaa na", "Please go")
        put("aa gaya", "Arrived / Came")
        put("aa gayi", "Arrived / Came")
        put("aa gaye", "Arrived / Came")
        put("chala gaya", "Went away")
        put("chali gayi", "Went away")
        put("khatam", "Finished")
        put("khatam ho gaya", "It is finished")
        put("shuru karo", "Start")
        put("shuru ho gaya", "It started")
        put("band karo", "Stop / Close")
        put("kholo", "Open")
        put("khol do", "Open it")
        put("band karo na", "Please stop")
        put("hao", "Okay / Done")
        put("pakka nahi", "Not sure")
        put("shayad", "Maybe / Perhaps")
        put("ho sakta hai", "Maybe / It can happen")
        put("nahi ho sakta", "Cannot happen / Impossible")
        put("zaroor", "Definitely / Surely")
        put("hamesha", "Always")
        put("kabhi nahi", "Never")
        put("fir se", "Again")
        put("ek baar", "Once")
        put("do baar", "Twice")
        put("teen baar", "Three times")
        put("kitni baar", "How many times")
        put("ro roz", "Every day / Daily")
        put("har roz", "Every day")
        put("har din", "Every day")
        put("hamesha ki tarah", "As always")
        put("ek dusra", "Each other")
        put("aapas mein", "Among ourselves")
        put("saath mein", "Together")
        put("sab log", "Everyone")
        put("sabji", "Get it / Understood")
        put("pata hai", "I know")
        put("pata nahi", "I do not know")
        put("mujhe pata hai", "I know")
        put("mujhe nahi pata", "I do not know")
        put("mujhe malum hai", "I know")
        put("mujhe malum nahi", "I do not know")
        put("pata chal gaya", "Found out")
        put("pata chala", "Came to know")
        put("yaad hai", "I remember")
        put("yaad nahi", "I don't remember")
        put("bhool gaya", "Forgot")
        put("bhool gayi", "Forgot")
        put("yaad aa gaya", "Remembered")
        put("yaad dilao", "Remind me")
        put("intezaar hai", "I am waiting")
        put("wait karo", "Wait")
        put("wait kar raha hoon", "I am waiting")
        put("wait kar rahi hoon", "I am waiting")
        put("intazaar kar raha hoon", "I am waiting")
        put("aapke liye", "For you")
        put("tumhare liye", "For you")
        put("mere liye", "For me")
        put("humare liye", "For us")
        put("unke liye", "For them")

        // ─── Progressive Verb Phrases (main word book raha hoon) ───
        put("main padh raha hoon", "I am studying")
        put("main padh rahi hoon", "I am studying")
        put("main likh raha hoon", "I am writing")
        put("main likh rahi hoon", "I am writing")
        put("main so raha hoon", "I am sleeping")
        put("main so rahi hoon", "I am sleeping")
        put("main khel raha hoon", "I am playing")
        put("main khel rahi hoon", "I am playing")
        put("main ga raha hoon", "I am singing")
        put("main ga rahi hoon", "I am singing")
        put("main nach raha hoon", "I am dancing")
        put("main nach rahi hoon", "I am dancing")
        put("main daud raha hoon", "I am running")
        put("main daud rahi hoon", "I am running")
        put("main taira raha hoon", "I am swimming")
        put("main taira rahi hoon", "I am swimming")
        put("main khana kha raha hoon", "I am eating food")
        put("main khana kha rahi hoon", "I am eating food")
        put("main pani pee raha hoon", "I am drinking water")
        put("main pani pee rahi hoon", "I am drinking water")
        put("main TV dekh raha hoon", "I am watching TV")
        put("main TV dekh rahi hoon", "I am watching TV")
        put("main book padh raha hoon", "I am reading a book")
        put("main book padh rahi hoon", "I am reading a book")
        put("main movie dekh raha hoon", "I am watching a movie")
        put("main movie dekh rahi hoon", "I am watching a movie")
        put("main gana sun raha hoon", "I am listening to music")
        put("main gana sun rahi hoon", "I am listening to music")
        put("main phone par baat kar raha hoon", "I am talking on the phone")
        put("main phone par baat kar rahi hoon", "I am talking on the phone")
        put("main kapde dho raha hoon", "I am washing clothes")
        put("main kapde dho rahi hoon", "I am washing clothes")
        put("main bartan dho raha hoon", "I am washing dishes")
        put("main bartan dho rahi hoon", "I am washing dishes")
        put("main jhadoo laga raha hoon", "I am sweeping")
        put("main jhadoo laga rahi hoon", "I am sweeping")
        put("main khana bana raha hoon", "I am cooking food")
        put("main khana bana rahi hoon", "I am cooking food")
        put("main nashta kar raha hoon", "I am having breakfast")
        put("main nashta kar rahi hoon", "I am having breakfast")
        put("main exercise kar raha hoon", "I am exercising")
        put("main exercise kar rahi hoon", "I am exercising")
        put("main yoga kar raha hoon", "I am doing yoga")
        put("main yoga kar rahi hoon", "I am doing yoga")
        put("main drawing bana raha hoon", "I am drawing")
        put("main drawing bana rahi hoon", "I am drawing")

        // ─── Progressive with other subjects ───
        put("aap kya kar rahe hain", "What are you doing")
        put("tum kya kar rahe ho", "What are you doing")
        put("woh kya kar raha hai", "What is he doing")
        put("woh kya kar rahi hai", "What is she doing")
        put("woh so raha hai", "He is sleeping")
        put("woh so rahi hai", "She is sleeping")
        put("woh padh raha hai", "He is studying")
        put("woh padh rahi hai", "She is studying")
        put("woh khel raha hai", "He is playing")
        put("woh khel rahi hai", "She is playing")
        put("bache khel rahe hain", "The children are playing")
        put("sab log khel rahe hain", "Everyone is playing")

        // ─── Past tense ───
        put("main gaya tha", "I went")
        put("main gayi thi", "I went")
        put("main gaya", "I went")
        put("main gayi", "I went")
        put("main aaya tha", "I came")
        put("main aayi thi", "I came")
        put("main khaya", "I ate")
        put("main piya", "I drank")
        put("main soya", "I slept")
        put("main so gayi", "I slept")
        put("main padha", "I studied")
        put("main likha", "I wrote")
        put("main khela", "I played")
        put("main dauda", "I ran")
        put("main nacha", "I danced")
        put("main gaya hoon", "I have gone")
        put("main aaya hoon", "I have come")
        put("main kar liya", "I have done it")
        put("main kha liya", "I have eaten")
        put("main pee liya", "I have drunk")
        put("main padh liya", "I have studied")
        put("main likh diya", "I have written it")
        put("main dekh liya", "I have seen it")
        put("main samajh gaya", "I understood")
        put("main samajh gayi", "I understood")
        put("aap samajh gaye", "You understood")
        put("tum samajh gaye", "You understood")

        // ─── Future tense ───
        put("main aaunga", "I will come")
        put("main aaungi", "I will come")
        put("main jaunga", "I will go")
        put("main jaungi", "I will go")
        put("main karunga", "I will do it")
        put("main karungi", "I will do it")
        put("main khau ga", "I will eat")
        put("main khau gi", "I will eat")
        put("main piiunga", "I will drink")
        put("main piiungi", "I will drink")
        put("main dekhunga", "I will see")
        put("main dekhungi", "I will see")
        put("main sochunga", "I will think")
        put("main sochungi", "I will think")
        put("main puchunga", "I will ask")
        put("main puchungi", "I will ask")
        put("main bolunga", "I will speak")
        put("main bolungi", "I will speak")
        put("main bataunga", "I will tell")
        put("main bataungi", "I will tell")
        put("main kal aaunga", "I will come tomorrow")
        put("main kal aaungi", "I will come tomorrow")
        put("main kal jaunga", "I will go tomorrow")
        put("main kal jaungi", "I will go tomorrow")
        put("aap kab aaonge", "When will you come")
        put("aap kab jaonge", "When will you go")
        put("tum kab aaoge", "When will you come")
        put("tum kab aaogi", "When will you come")
        put("kya aap aaonge", "Will you come")
        put("kya aap aaongi", "Will you come")
        put("kya tum aaoge", "Will you come")
        put("kya tum aaogi", "Will you come")

        // ─── Must / Should / Can ───
        put("mujhe jaana hai", "I have to go")
        put("mujhe aana hai", "I have to come")
        put("mujhe karna hai", "I have to do it")
        put("mujhe khana hai", "I have to eat")
        put("mujhe padhna hai", "I have to study")
        put("mujhe sona hai", "I have to sleep")
        put("mujhe bolna hai", "I have to speak")
        put("aapko jaana hai", "You have to go")
        put("aapko karna hai", "You have to do it")
        put("mujhe nahi jaana", "I do not want to go")
        put("mujhe nahi karna", "I do not want to do it")
        put("aap kya chahte hain", "What do you want")
        put("tum kya chahte ho", "What do you want")
        put("aap kya chahti hain", "What do you want")
        put("tum kya chahti ho", "What do you want")
        put("mujhe ye chahiye", "I want this")
        put("mujhe woh chahiye", "I want that")
        put("aapko kya chahiye", "What do you want")
        put("tumhe kya chahiye", "What do you want")

        // ─── Comparisons ───
        put("ye usse achha hai", "This is better than that")
        put("ye sabse achha hai", "This is the best")
        put("utna achha nahi", "Not as good")
        put("is se bada", "Bigger than this")
        put("sabse bada", "The biggest")
        put("bahut bada", "Very big")
        put("thoda chota", "A little small")
        put("bahut chota", "Very small")
        put("zyada achha", "Better")
        put("kam achha", "Less good")
        put("zyada mehnga", "More expensive")
        put("kam mehnga", "Less expensive")
        put("zyada tez", "Faster")
        put("zyada dheere", "Slower")

        // ─── Reasons & Explanations ───
        put("isliye", "That is why")
        put("isliye main aaya", "That is why I came")
        put("isliye main gaya", "That is why I went")
        put("isliye main nahi aaya", "That is why I did not come")
        put("kyonki main busy tha", "Because I was busy")
        put("kyonki mere paas time nahi tha", "Because I did not have time")
        put("kyonki main bimaar tha", "Because I was sick")
        put("kyonki main bimaar thi", "Because I was sick")
        put("wajah kya hai", "What is the reason")
        put("iska matlab", "This means")
        put("matlab ki", "Meaning that")
        put("matlab ye hai ki", "The meaning is that")
        put("aapka matlab kya hai", "What do you mean")
        put("tumhara matlab kya hai", "What do you mean")

        // ─── Miscellaneous Common ───
        put("aur kya", "What else")
        put("aur kuch", "Something else")
        put("aur koi", "Someone else")
        put("aur kahan", "Where else")
        put("aur kab", "When else")
        put("aur kaun", "Who else")
        put("iske alawa", "Apart from this")
        put("iske baad", "After this")
        put("iske pehle", "Before this")
        put("is waqt", "At this moment")
        put("us waqt", "At that moment")
        put("kisi waqt", "At some point")
        put("ek saath", "All together")
        put("ek baar mein", "At once")
        put("dheere dheere", "Slowly")
        put("aahista aahista", "Slowly")
        put("jaldi jaldi", "Quickly")
        put("ek dum", "Completely")
        put("bilkul sahi", "Absolutely right")
        put("bilkul galat", "Absolutely wrong")
        put("thoda sa", "A little bit")
        put("bahut saara", "A lot")
        put("kaafi kuch", "Quite a lot")
        put("ito", "That much")
        put("utna", "That many")
        put("jitna", "As much")
        put("jitna ho sake", "As much as possible")
        put("jaldi se", "Quickly")
        put("dheere se", "Slowly")
        put("ache se", "Properly")
        put("dhyan se", "Carefully")
        put("accidentally", "Accidentally")
        put("purposefully", "On purpose")
        put("jaan boojh kar", "On purpose")
        put("ghalti se", "By mistake")
        put("pakka plan", "Definite plan")
        put("final ho gaya", "It is finalized")
        put("confirm hai", "It is confirmed")
        put("confirm karo", "Please confirm")
        put("double check karo", "Double check")
        put("soch lo", "Think about it")
        put("soch raha hoon", "I am thinking")
        put("soch rahi hoon", "I am thinking")
        put("decision le lo", "Make a decision")
        put("decide ho gaya", "It is decided")
        put("sheher", "City")
        put("gaon", "Village")
        put("shahar", "City")
        put("mohalla", "Neighborhood")
        put("gali", "Street")
        put("rasta", "Road")
        put("sadak", "Road")
        put("pul", "Bridge")
        put("nadi", "River")
        put("pahaad", "Mountain")
        put("samundar", "Ocean")
        put("jungle", "Forest")
        put("bagh", "Garden")
        put("badiya", "Great")
        put("bahut badiya", "Very great")
        put("kamaal kar diya", "You did amazing")
        put("shabash", "Well done")
        put("waah kya baat hai", "Wow that is great")
        put("maze kar", "Have fun")
        put("maze karo", "Have fun")
        put("enjoy karo", "Enjoy")
        put("aram karo", "Take rest")
        put("chill karo", "Take it easy")
        put("tension mat lo", "Do not stress")
        put("happiness", "Happiness")
        put("sukoon", "Peace")
        put("shanti", "Peace")
        put("aman", "Peace")
        put("dard", "Pain")
        put("takleef", "Problem")
        put("mushkil", "Difficult")
        put("aasan", "Easy")
        put("sambhav", "Possible")
        put("namumkin", "Impossible")
        put("hogi", "Will happen")
        put("ho jayega", "Will happen")
        put("nahi hoga", "Will not happen")
        put("ho sakta hai", "Maybe")
        put("nahi ho sakta", "Cannot happen")
        put("pakka hoga", "It surely will")
        put("shayad hoga", "Maybe it will")
    }

    private val templates: List<Pair<Regex, (MatchResult) -> String>> = listOf(
        // "mera naam {X} hai" → "My name is {X}"
        Regex("^mera naam (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "My name is ${m.groupValues[1]}"
        },
        // "mujhe {X} chahiye" → "I need {X}"
        Regex("^mujhe (.+) chahiye$", RegexOption.IGNORE_CASE) to { m ->
            "I need ${m.groupValues[1]}"
        },
        // "mujhe {X} nahi chahiye" → "I don't need {X}"
        Regex("^mujhe (.+) nahi chahiye$", RegexOption.IGNORE_CASE) to { m ->
            "I do not need ${m.groupValues[1]}"
        },
        // "mujhe {X} pasand hai" → "I like {X}"
        Regex("^mujhe (.+) pasand hai$", RegexOption.IGNORE_CASE) to { m ->
            "I like ${m.groupValues[1]}"
        },
        // "mujhe {X} pasand nahi" → "I don't like {X}"
        Regex("^mujhe (.+) pasand nahi$", RegexOption.IGNORE_CASE) to { m ->
            "I do not like ${m.groupValues[1]}"
        },
        // "main {X} ja raha hoon" → "I am going to {X}"
        Regex("^main (.+) ja raha hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am going to ${m.groupValues[1]}"
        },
        // "main {X} ja rahi hoon" → "I am going to {X}"
        Regex("^main (.+) ja rahi hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am going to ${m.groupValues[1]}"
        },
        // "mein {X} ja raha hoon" → "I am going to {X}"
        Regex("^mein (.+) ja raha hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am going to ${m.groupValues[1]}"
        },
        // "main {X} aa raha hoon" → "I am coming to {X}"
        Regex("^main (.+) aa raha hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am coming to ${m.groupValues[1]}"
        },
        // "main {X} kar raha hoon" → "I am doing {X}"
        Regex("^main (.+) kar raha hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am ${m.groupValues[1]}ing"
        },
        // "main {X} kar rahi hoon" → "I am doing {X}"
        Regex("^main (.+) kar rahi hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am ${m.groupValues[1]}ing"
        },
        // "aap {X} kar sakte hain" → "You can {X}"
        Regex("^aap (.+) kar sakte hain$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },
        // "tum {X} kar sakte ho" → "You can {X}"
        Regex("^tum (.+) kar sakte ho$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },
        // "aap {X} sakte hain" → "You can {X}"
        Regex("^aap (.+) sakte hain$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },
        // "kya aap {X}" → "Do you {X}"
        Regex("^kya aap (.+)$", RegexOption.IGNORE_CASE) to { m ->
            "Do you ${m.groupValues[1]}"
        },
        // "kya tum {X}" → "Do you {X}"
        Regex("^kya tum (.+)$", RegexOption.IGNORE_CASE) to { m ->
            "Do you ${m.groupValues[1]}"
        },
        // "{X} kahan hai" → "Where is {X}"
        Regex("^(.+) kahan hai$", RegexOption.IGNORE_CASE) to { m ->
            "Where is ${m.groupValues[1]}"
        },
        // "{X} kahan ho" → "Where are {X}"
        Regex("^(.+) kahan ho$", RegexOption.IGNORE_CASE) to { m ->
            "Where are ${m.groupValues[1]}"
        },
        // "{X} kab aa raha hai" → "When is {X} coming"
        Regex("^(.+) kab aa raha hai$", RegexOption.IGNORE_CASE) to { m ->
            "When is ${m.groupValues[1]} coming"
        },
        // "{X} kab aa rahi hai" → "When is {X} coming"
        Regex("^(.+) kab aa rahi hai$", RegexOption.IGNORE_CASE) to { m ->
            "When is ${m.groupValues[1]} coming"
        },
        // "{X} kyun nahi" → "Why not {X}"
        Regex("^(.+) kyun nahi$", RegexOption.IGNORE_CASE) to { m ->
            "Why not ${m.groupValues[1]}"
        },
        // "{X} kar do" → "Do {X}"
        Regex("^(.+) kar do$", RegexOption.IGNORE_CASE) to { m ->
            "Please ${m.groupValues[1]}"
        },
        // "{X} mat karo" → "Don't {X}"
        Regex("^(.+) mat karo$", RegexOption.IGNORE_CASE) to { m ->
            "Do not ${m.groupValues[1]}"
        },
        // "{X} lo" → "Take {X}"
        Regex("^(.+) lo$", RegexOption.IGNORE_CASE) to { m ->
            "Take ${m.groupValues[1]}"
        },
        // "{X} nahi hai" → "There is no {X}"
        Regex("^(.+) nahi hai$", RegexOption.IGNORE_CASE) to { m ->
            "There is no ${m.groupValues[1]}"
        },
        // "{X} aaya" → "{X} came"
        Regex("^(.+) aaya$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} came"
        },
        // "{X} aa gaya" → "{X} arrived"
        Regex("^(.+) aa gaya$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} arrived"
        },
        // "{X} aa gayi" → "{X} arrived"
        Regex("^(.+) aa gayi$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} arrived"
        },
        // "{X} ho gaya" → "{X} is done"
        Regex("^(.+) ho gaya$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} is done"
        },
        // "{X} karo" → "Do {X}"
        Regex("^(.+) karo$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]}"
        },
        // "{X} bhejo" → "Send {X}"
        Regex("^(.+) bhejo$", RegexOption.IGNORE_CASE) to { m ->
            "Send ${m.groupValues[1]}"
        },
        // "{X} lao" → "Bring {X}"
        Regex("^(.+) lao$", RegexOption.IGNORE_CASE) to { m ->
            "Bring ${m.groupValues[1]}"
        },
        // "{X} kha lo" → "Eat {X}"
        Regex("^(.+) kha lo$", RegexOption.IGNORE_CASE) to { m ->
            "Eat ${m.groupValues[1]}"
        },
        // "{X} pi lo" → "Drink {X}"
        Regex("^(.+) pi lo$", RegexOption.IGNORE_CASE) to { m ->
            "Drink ${m.groupValues[1]}"
        },
        // "{X} mein hai" → "{X} is in"
        Regex("^(.+) mein hai$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} is in"
        },
        // "{X} hai kya" → "Is {X}"
        Regex("^(.+) hai kya$", RegexOption.IGNORE_CASE) to { m ->
            "Is ${m.groupValues[1]}"
        },
        // "aaj {X} hai" → "Today is {X}"
        Regex("^aaj (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "Today is ${m.groupValues[1]}"
        },
        // "kal {X}" → "Tomorrow {X}"
        Regex("^kal (.+)$", RegexOption.IGNORE_CASE) to { m ->
            "Tomorrow ${m.groupValues[1]}"
        },
        // "aapka {X} kaise hai" → "How is your {X}"
        Regex("^aapka (.+) kaise hai$", RegexOption.IGNORE_CASE) to { m ->
            "How is your ${m.groupValues[1]}"
        },
        // "tumhara {X} kaise hai" → "How is your {X}"
        Regex("^tumhara (.+) kaise hai$", RegexOption.IGNORE_CASE) to { m ->
            "How is your ${m.groupValues[1]}"
        },
        // "kitna {X} hai" → "How much {X} is there"
        Regex("^kitna (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "How much ${m.groupValues[1]} is there"
        },
        // "bahut {X} hai" → "There is a lot of {X}"
        Regex("^bahut (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "There is a lot of ${m.groupValues[1]}"
        },

        // "main {X} raha hoon" → "I am {X}ing" (catch-all progressive)
        Regex("^main ([a-z]+) raha hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am ${m.groupValues[1]}ing"
        },
        // "main {X} rahi hoon" → "I am {X}ing"
        Regex("^main ([a-z]+) rahi hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I am ${m.groupValues[1]}ing"
        },
        // "woh {X} raha hai" → "He is {X}ing"
        Regex("^woh ([a-z]+) raha hai$", RegexOption.IGNORE_CASE) to { m ->
            "He is ${m.groupValues[1]}ing"
        },
        // "woh {X} rahi hai" → "She is {X}ing"
        Regex("^woh ([a-z]+) rahi hai$", RegexOption.IGNORE_CASE) to { m ->
            "She is ${m.groupValues[1]}ing"
        },
        // "woh {X} rahe hain" → "They are {X}ing"
        Regex("^woh ([a-z]+) rahe hain$", RegexOption.IGNORE_CASE) to { m ->
            "They are ${m.groupValues[1]}ing"
        },
        // "aap {X} rahe hain" → "You are {X}ing"
        Regex("^aap ([a-z]+) rahe hain$", RegexOption.IGNORE_CASE) to { m ->
            "You are ${m.groupValues[1]}ing"
        },
        // "tum {X} rahe ho" → "You are {X}ing"
        Regex("^tum ([a-z]+) rahe ho$", RegexOption.IGNORE_CASE) to { m ->
            "You are ${m.groupValues[1]}ing"
        },
        // "bache {X} rahe hain" → "Children are {X}ing"
        Regex("^bache ([a-z]+) rahe hain$", RegexOption.IGNORE_CASE) to { m ->
            "Children are ${m.groupValues[1]}ing"
        },
        // "log {X} rahe hain" → "People are {X}ing"
        Regex("^log ([a-z]+) rahe hain$", RegexOption.IGNORE_CASE) to { m ->
            "People are ${m.groupValues[1]}ing"
        },

        // "main {X} gaya" → "I {X}ed" (past)
        Regex("^main ([a-z]+) gaya$", RegexOption.IGNORE_CASE) to { m ->
            "I ${m.groupValues[1]}ed"
        },
        // "main {X} gayi" → "I {X}ed"
        Regex("^main ([a-z]+) gayi$", RegexOption.IGNORE_CASE) to { m ->
            "I ${m.groupValues[1]}ed"
        },
        // "woh {X} gaya" → "He {X}ed"
        Regex("^woh ([a-z]+) gaya$", RegexOption.IGNORE_CASE) to { m ->
            "He ${m.groupValues[1]}ed"
        },
        // "woh {X} gayi" → "She {X}ed"
        Regex("^woh ([a-z]+) gayi$", RegexOption.IGNORE_CASE) to { m ->
            "She ${m.groupValues[1]}ed"
        },

        // "main {X} sakta hoon" → "I can {X}"
        Regex("^main ([a-z]+) sakta hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I can ${m.groupValues[1]}"
        },
        // "main {X} sakti hoon" → "I can {X}"
        Regex("^main ([a-z]+) sakti hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I can ${m.groupValues[1]}"
        },
        // "aap {X} sakte hain" → "You can {X}"
        Regex("^aap ([a-z]+) sakte hain$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },
        // "aap {X} sakti hain" → "You can {X}"
        Regex("^aap ([a-z]+) sakti hain$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },

        // "{X} mat karo" → "Do not {X}"
        Regex("^([a-z]+) mat karo$", RegexOption.IGNORE_CASE) to { m ->
            "Do not ${m.groupValues[1]}"
        },
        // "{X} mat kar" → "Do not {X}"
        Regex("^([a-z]+) mat kar$", RegexOption.IGNORE_CASE) to { m ->
            "Do not ${m.groupValues[1]}"
        },
        // "{X} nahi karo" → "Do not do {X}"
        Regex("^([a-z]+) nahi karo$", RegexOption.IGNORE_CASE) to { m ->
            "Do not do ${m.groupValues[1]}"
        },

        // "kya {X} hai" → "What is {X}"
        Regex("^kya (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "What is ${m.groupValues[1]}"
        },
        // "kaun {X} hai" → "Who is {X}"
        Regex("^kaun (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "Who is ${m.groupValues[1]}"
        },
        // "kab {X} hai" → "When is {X}"
        Regex("^kab (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "When is ${m.groupValues[1]}"
        },
        // "kahan {X} hai" → "Where is the {X}"
        Regex("^kahan (.+) hai$", RegexOption.IGNORE_CASE) to { m ->
            "Where is the ${m.groupValues[1]}"
        },

        // "kya aap {X} sakte hain" → "Can you {X}"
        Regex("^kya aap (.+) sakte hain$", RegexOption.IGNORE_CASE) to { m ->
            "Can you ${m.groupValues[1]}"
        },
        // "kya aap {X} sakti hain" → "Can you {X}"
        Regex("^kya aap (.+) sakti hain$", RegexOption.IGNORE_CASE) to { m ->
            "Can you ${m.groupValues[1]}"
        },
        // "kya tum {X} sakte ho" → "Can you {X}"
        Regex("^kya tum (.+) sakte ho$", RegexOption.IGNORE_CASE) to { m ->
            "Can you ${m.groupValues[1]}"
        },

        // "main {X} kar sakta hoon" → "I can {X}"
        Regex("^main ([a-z]+) kar sakta hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I can ${m.groupValues[1]}"
        },
        // "main {X} kar sakti hoon" → "I can {X}"
        Regex("^main ([a-z]+) kar sakti hoon$", RegexOption.IGNORE_CASE) to { m ->
            "I can ${m.groupValues[1]}"
        },
        // "aap {X} kar sakte hain" → "You can {X}"
        Regex("^aap ([a-z]+) kar sakte hain$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },
        // "tum {X} kar sakte ho" → "You can {X}"
        Regex("^tum ([a-z]+) kar sakte ho$", RegexOption.IGNORE_CASE) to { m ->
            "You can ${m.groupValues[1]}"
        },

        // "{X} khatam ho gaya" → "{X} is finished"
        Regex("^(.+) khatam ho gaya$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} is finished"
        },
        // "{X} khatam ho gayi" → "{X} is finished"
        Regex("^(.+) khatam ho gayi$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} is finished"
        },
        // "{X} shuru ho gaya" → "{X} has started"
        Regex("^(.+) shuru ho gaya$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} has started"
        },
        // "{X} shuru ho gayi" → "{X} has started"
        Regex("^(.+) shuru ho gayi$", RegexOption.IGNORE_CASE) to { m ->
            "${m.groupValues[1]} has started"
        },

        // "maine {X} kiya" → "I did {X}"
        Regex("^maine (.+) kiya$", RegexOption.IGNORE_CASE) to { m ->
            "I did ${m.groupValues[1]}"
        },
        // "kya aapne {X} dekha" → "Did you see {X}"
        Regex("^kya aapne (.+) dekha$", RegexOption.IGNORE_CASE) to { m ->
            "Did you see ${m.groupValues[1]}"
        },
        // "kya aapne {X} suna" → "Did you hear {X}"
        Regex("^kya aapne (.+) suna$", RegexOption.IGNORE_CASE) to { m ->
            "Did you hear ${m.groupValues[1]}"
        },

        // "mujhe {X} ka darr hai" → "I am afraid of {X}"
        Regex("^mujhe (.+) ka darr hai$", RegexOption.IGNORE_CASE) to { m ->
            "I am afraid of ${m.groupValues[1]}"
        },
        // "mujhe {X} pyaar hai" → "I love {X}"
        Regex("^mujhe (.+) pyaar hai$", RegexOption.IGNORE_CASE) to { m ->
            "I love ${m.groupValues[1]}"
        },
        // "mujhe {X} se nafrat hai" → "I hate {X}"
        Regex("^mujhe (.+) se nafrat hai$", RegexOption.IGNORE_CASE) to { m ->
            "I hate ${m.groupValues[1]}"
        },

        // "{X} ka kya hua" → "What happened to {X}"
        Regex("^(.+) ka kya hua$", RegexOption.IGNORE_CASE) to { m ->
            "What happened to ${m.groupValues[1]}"
        },
        // "{X} ki kya hui" → "What happened to {X}"
        Regex("^(.+) ki kya hui$", RegexOption.IGNORE_CASE) to { m ->
            "What happened to ${m.groupValues[1]}"
        },

        // "mujhe nahi pata" → "I don't know"
        Regex("^mujhe nahi pata$", RegexOption.IGNORE_CASE) to { m ->
            "I do not know"
        },
        // "mujhe nahi malum" → "I don't know"
        Regex("^mujhe nahi malum$", RegexOption.IGNORE_CASE) to { m ->
            "I do not know"
        },
    )

    private val wordDictionary: Map<String, String> = buildMap {
        put("aaj", "today")
        put("kal", "tomorrow")
        put("parson", "day after tomorrow")
        put("aajkal", "nowadays")
        put("abhi", "now")
        put("ab", "now")
        put("phir", "again")
        put("fir", "again")
        put("hamesha", "always")
        put("kabhi", "sometimes")
        put("kabhi nahi", "never")
        put("roz", "daily")
        put("har", "every")
        put("har roz", "every day")
        put("har din", "every day")
        put("din", "day")
        put("raat", "night")
        put("subah", "morning")
        put("shaam", "evening")
        put("dopahar", "afternoon")
        put("sawere", "early morning")
        put("raat ko", "at night")
        put("subah subah", "early morning")
        put("der raat", "late night")

        put("main", "I")
        put("mai", "I")
        put("mein", "I")
        put("mujhe", "I")
        put("mera", "my")
        put("meri", "my")
        put("mere", "my")
        put("mujhko", "me")
        put("apna", "my own")
        put("apni", "my own")
        put("apne", "my own")

        put("tum", "you")
        put("tumhara", "your")
        put("tumhari", "your")
        put("tumhare", "your")
        put("tumhe", "you")
        put("tujhe", "you")
        put("tu", "you")
        put("teri", "your")
        put("tera", "your")
        put("tere", "your")

        put("aap", "you")
        put("aapka", "your")
        put("aapki", "your")
        put("aapke", "your")
        put("aapko", "you")
        put("aapse", "from you")

        put("woh", "that, they")
        put("wo", "that")
        put("voh", "that")
        put("unka", "their")
        put("unki", "their")
        put("unke", "their")
        put("unko", "them")
        put("unhone", "they")
        put("unka", "their")
        put("unke", "their")
        put("unki", "their")

        put("ye", "this")
        put("yah", "this")
        put("yeh", "this")
        put("iska", "its")
        put("iski", "its")
        put("is", "this")
        put("inse", "from this")
        put("inhe", "these")

        put("sab", "everyone")
        put("log", "people")
        put("sab log", "everyone")
        put("kuch", "something")
        put("kuch nahi", "nothing")
        put("kuch bhi", "anything")
        put("koi", "someone")
        put("kisi", "someone")
        put("kisi ko", "someone")
        put("kisi ne", "someone")
        put("kisi ka", "someone's")
        put("kisi ki", "someone's")
        put("kisi ke", "someone's")

        put("aur", "and, more")
        put("lekin", "but")
        put("magar", "but")
        put("parantu", "however")
        put("ya", "or")
        put("toh", "so")
        put("isliye", "therefore")
        put("kyonki", "because")
        put("jab", "when")
        put("agar", "if")
        put("to", "then")
        put("bhi", "also")
        put("hi", "only")

        put("achha", "good")
        put("achhi", "good")
        put("kharab", "bad")
        put("bura", "bad")
        put("bure", "bad")
        put("buri", "bad")
        put("sahi", "correct")
        put("galat", "wrong")
        put("thoda", "little")
        put("thodi", "little")
        put("thode", "some")
        put("kuch", "some")
        put("bahut", "very")
        put("zyada", "too much")
        put("jada", "too much")
        put("kaafi", "enough")
        put("poora", "complete")
        put("puri", "complete")
        put("sabse", "most")
        put("kam", "less")
        put("kum", "less")

        put("jaana", "go")
        put("jana", "go")
        put("jaa", "go")
        put("ja", "go")
        put("jata", "goes")
        put("jao", "go")
        put("gaya", "went")
        put("gayi", "went")
        put("gaye", "went")
        put("ja raha", "going")
        put("ja rahi", "going")
        put("ja rahe", "going")
        put("aana", "come")
        put("aa", "come")
        put("aao", "come")
        put("aaya", "came")
        put("aayi", "came")
        put("aaye", "came")
        put("aa raha", "coming")
        put("aa rahi", "coming")
        put("aa rahe", "coming")
        put("karna", "do")
        put("kar", "do")
        put("karo", "do")
        put("kiya", "did")
        put("karte", "do")
        put("karta", "does")
        put("karti", "does")
        put("kar raha", "doing")
        put("kar rahi", "doing")
        put("kar rahe", "doing")
        put("karke", "after doing")
        put("karne", "to do")
        put("kar lo", "do it")
        put("kar liya", "done")

        put("khana", "eat")
        put("kha", "eat")
        put("khao", "eat")
        put("khaya", "ate")
        put("kha liya", "ate")
        put("kha lo", "eat")
        put("kha raha", "eating")
        put("kha rahi", "eating")
        put("peena", "drink")
        put("pi", "drink")
        put("piyo", "drink")
        put("piya", "drank")
        put("pee", "drink")
        put("pee liya", "drank")
        put("pi liya", "drank")
        put("pe raha", "drinking")
        put("pe rahi", "drinking")

        put("bolna", "speak")
        put("bol", "speak")
        put("bolo", "speak")
        put("bola", "said")
        put("boli", "said")
        put("bole", "said")
        put("bol raha", "saying")
        put("bol rahi", "saying")
        put("kehna", "say")
        put("keh", "say")
        put("kaho", "say")
        put("kaha", "said")
        put("keh raha", "saying")
        put("keh rahi", "saying")
        put("sunna", "listen")
        put("sun", "listen")
        put("suno", "listen")
        put("suna", "heard")
        put("sun raha", "listening")
        put("sun rahi", "listening")
        put("dekhna", "see")
        put("dekh", "see")
        put("dekho", "see")
        put("dekha", "saw")
        put("dekh raha", "seeing")
        put("dekh rahi", "seeing")
        put("dekh lo", "look")

        put("batana", "tell")
        put("bata", "tell")
        put("batao", "tell")
        put("bataya", "told")
        put("bata raha", "telling")
        put("bata rahi", "telling")
        put("puchna", "ask")
        put("puch", "ask")
        put("pucho", "ask")
        put("pucha", "asked")
        put("puch raha", "asking")
        put("puch rahi", "asking")

        put("dena", "give")
        put("de", "give")
        put("do", "give")
        put("diyo", "give")
        put("diya", "gave")
        put("dene", "to give")
        put("de do", "give")
        put("lena", "take")
        put("le", "take")
        put("lo", "take")
        put("liya", "took")
        put("liye", "took")
        put("le lo", "take")
        put("laana", "bring")
        put("lao", "bring")
        put("laya", "brought")
        put("le aana", "bring")
        put("le aao", "bring")
        put("le jaana", "take away")
        put("le jao", "take away")
        put("bhejna", "send")
        put("bhej", "send")
        put("bhejo", "send")
        put("bheja", "sent")
        put("bhej do", "send")
        put("raakhna", "keep")
        put("rakh", "keep")
        put("rakho", "keep")
        put("rakha", "kept")
        put("rakh lo", "keep")
        put("dhona", "wash")
        put("dho", "wash")
        put("dho lo", "wash")

        put("samajhna", "understand")
        put("samajh", "understand")
        put("samjha", "understood")
        put("samajh lo", "understand")
        put("samajh mein aaya", "understood")
        put("samajh nahi aaya", "did not understand")
        put("pata", "known")
        put("pata hai", "know")
        put("pata nahi", "do not know")
        put("malum", "known")
        put("malum hai", "know")
        put("malum nahi", "do not know")
        put("yaad", "remember")
        put("yaad hai", "remember")
        put("yaad nahi", "do not remember")
        put("bhoolna", "forget")
        put("bhool", "forget")
        put("bhoolo", "forget")
        put("bhool gaya", "forgot")
        put("bhool gayi", "forgot")

        put("sach", "truth")
        put("sach mein", "really")
        put("sachchi", "really")
        put("jhooth", "lie")
        put("pakka", "sure")
        put("shayad", "maybe")
        put("zaroor", "definitely")
        put("ho sakta hai", "maybe")
        put("nahi ho sakta", "impossible")
        put("sambhav", "possible")
        put("asambhav", "impossible")

        put("yahan", "here")
        put("wahan", "there")
        put("idhar", "here")
        put("udhar", "there")
        put("kahan", "where")
        put("kahan se", "from where")
        put("kahan tak", "up to where")
        put("pass", "near")
        put("paas", "near")
        put("nazdeek", "close")
        put("dur", "far")
        put("door", "far")
        put("andar", "inside")
        put("bahar", "outside")
        put("upar", "above")
        put("neeche", "below")
        put("aage", "ahead")
        put("piche", "behind")
        put("samne", "in front")
        put("baayein", "left")
        put("dayein", "right")
        put("seedha", "straight")
        put("saamne", "in front")
        put("bich mein", "in the middle")

        put("kya", "what")
        put("kaun", "who")
        put("kab", "when")
        put("kahan", "where")
        put("kyon", "why")
        put("kyun", "why")
        put("kaise", "how")
        put("kis tarah", "how")
        put("kitna", "how much")
        put("kitne", "how many")
        put("kiska", "whose")
        put("kaunsa", "which")
        put("konsa", "which")
        put("kis liye", "for what")
        put("kiske liye", "for whom")

        put("haan", "yes")
        put("nahi", "no")
        put("na", "no")
        put("mat", "do not")
        put("bilkul", "absolutely")
        put("theek", "okay")
        put("thik", "okay")
        put("theek hai", "okay")
        put("achha", "okay")
        put("bas", "enough")
        put("chalo", "let's go")
        put("chalein", "let's go")
        put("ruko", "wait")
        put("thamo", "stop")
        put("bas karo", "stop")
        put("chup", "quiet")
        put("chup raho", "be quiet")

        put("mera naam", "my name")
        put("aapka naam", "your name")
        put("kya naam hai", "what is the name")
        put("umra", "age")
        put("kitni umra", "how old")
        put("salamat", "safe")
        put("surakshit", "safe")
        put("khatarnak", "dangerous")
        put("aasan", "easy")
        put("mushkil", "difficult")
        put("sasta", "cheap")
        put("mehnga", "expensive")
        put("achha", "good")
        put("behatar", "better")
        put("sabse achha", "best")
        put("tez", "fast")
        put("dheere", "slow")
        put("dheere dheere", "slowly")
        put("jaldi", "quickly")
        put("ekdam", "completely")
        put("thoda thoda", "gradually")

        put("parivaar", "family")
        put("ghar", "house")
        put("dost", "friend")
        put("doston", "friends")
        put("yaar", "friend")
        put("mitra", "friend")
        put("pati", "husband")
        put("patni", "wife")
        put("bache", "children")
        put("bachche", "children")
        put("bachcha", "child")
        put("bachchi", "girl child")
        put("ladka", "boy")
        put("ladki", "girl")
        put("admi", "man")
        put("aurat", "woman")
        put("log", "people")
        put("insaan", "human")
        put("vyakti", "person")
        put("buzurg", "elderly")

        put("paani", "water")
        put("doodh", "milk")
        put("chai", "tea")
        put("coffee", "coffee")
        put("roti", "bread")
        put("chawal", "rice")
        put("dal", "lentils")
        put("sabzi", "vegetables")
        put("phal", "fruit")
        put("murgi", "chicken")
        put("machhli", "fish")
        put("anda", "egg")
        put("makhan", "butter")
        put("tel", "oil")
        put("namak", "salt")
        put("shakkar", "sugar")
        put("chini", "sugar")
        put("mirchi", "chilli")

        put("kitaab", "book")
        put("pustak", "book")
        put("school", "school")
        put("college", "college")
        put("vidyalaya", "school")
        put("madad", "help")
        put("sahayata", "help")
        put("kaam", "work")
        put("naukri", "job")
        put("pesha", "profession")
        put("paisa", "money")
        put("paise", "money")
        put("rupaya", "rupee")
        put("kamai", "earnings")
        put("tankhwah", "salary")
        put("vetan", "salary")

        put("doctor", "doctor")
        put("dawai", "medicine")
        put("ilaaj", "treatment")
        put("hospital", "hospital")
        put("bimaari", "illness")
        put("rog", "disease")
        put("chot", "injury")
        put("dard", "pain")
        put("dard hai", "there is pain")
        put("sir", "head")
        put("pet", "stomach")
        put("peth", "stomach")
        put("haath", "hand")
        put("pair", "foot")
        put("aankh", "eye")
        put("khaana", "skin")
        put("khoon", "blood")
        put("bukhar", "fever")
        put("thand", "cold")
        put("khansi", "cough")
        put("zukaam", "cold")
        put("operation", "surgery")

        put("mobile", "phone")
        put("phone", "phone")
        put("phoon", "phone")
        put("computer", "computer")
        put("laptop", "laptop")
        put("internet", "internet")
        put("network", "network")
        put("battery", "battery")
        put("charger", "charger")
        put("wifi", "WiFi")
        put("data", "data")
        put("message", "message")
        put("photo", "photo")
        put("video", "video")
        put("number", "number")
        put("address", "address")
        put("location", "location")

        put("sundar", "beautiful")
        put("pyara", "cute")
        put("pyari", "cute")
        put("khoobsurat", "beautiful")
        put("sunder", "beautiful")
        put("bday", "birthday")
        put("bday hai", "is birthday")
        put("shaadi", "marriage")
        put("shadi", "marriage")
        put("vivaah", "marriage")
        put("party", "party")
        put("mehman", "guest")
        put("nyota", "invitation")
        put("dawat", "feast")
        put("tyohar", "festival")
        put("chutti", "holiday")
        put("chhutti", "holiday")
        put("safar", "journey")
        put("yatra", "trip")
        put("sair", "walk")
        put("ghoomna", "travel")
        put("ghoomo", "travel")
        put("picnic", "picnic")
        put("trip", "trip")
        put("plan", "plan")
        put("program", "program")
        put("karya", "task")
        put("karya kram", "schedule")

        // ─── People & Relationships ───
        put("mummy", "mom")
        put("papa", "dad")
        put("maa", "mom")
        put("bhai", "brother")
        put("bhaiya", "brother")
        put("behen", "sister")
        put("didi", "sister")
        put("chacha", "uncle")
        put("chachi", "aunt")
        put("mama", "uncle")
        put("mami", "aunt")
        put("nana", "grandfather")
        put("nani", "grandmother")
        put("bua", "aunt")
        put("tau", "uncle")
        put("tai", "aunt")
        put("jija", "brother in law")
        put("bhabhi", "sister in law")
        put("beta", "son")
        put("beti", "daughter")
        put("pati", "husband")
        put("patni", "wife")
        put("biwi", "wife")
        put("saas", "mother in law")
        put("sasur", "father in law")
        put("dada", "grandfather")
        put("dadi", "grandmother")
        put("bete", "sons")
        put("betiyan", "daughters")
        put("dono", "both")
        put("sabhi", "everyone")
        put("kisi", "someone")
        put("kisi ko", "to someone")
        put("kisi ka", "someone's")
        put("apne", "our own")
        put("khud", "self")
        put("khud se", "by self")
        put("aapas mein", "among ourselves")

        // ─── Common Places ───
        put("ghar", "home")
        put("school", "school")
        put("college", "college")
        put("office", "office")
        put("hospital", "hospital")
        put("bank", "bank")
        put("station", "station")
        put("airport", "airport")
        put("mandir", "temple")
        put("masjid", "mosque")
        put("gurudwara", "gurudwara")
        put("church", "church")
        put("park", "park")
        put("garden", "garden")
        put("bazaar", "market")
        put("market", "market")
        put("dukan", "shop")
        put("restaurant", "restaurant")
        put("hotel", "hotel")
        put("cinema", "cinema")
        put("museum", "museum")
        put("library", "library")
        put("gym", "gym")
        put("swimming pool", "swimming pool")
        put("playground", "playground")
        put("maidan", "ground")
        put("chauraha", "crossing")
        put("chowk", "square")
        put("nagar", "town")
        put("gaon", "village")
        put("dilli", "Delhi")
        put("mumbai", "Mumbai")
        put("pune", "Pune")
        put("kolkata", "Kolkata")
        put("chennai", "Chennai")
        put("bangalore", "Bangalore")
        put("hyderabad", "Hyderabad")
        put("ahmedabad", "Ahmedabad")
        put("jaipur", "Jaipur")
        put("lucknow", "Lucknow")
        put("bihar", "Bihar")
        put("up", "Uttar Pradesh")
        put("rajasthan", "Rajasthan")
        put("gujarat", "Gujarat")
        put("punjab", "Punjab")
        put("kerala", "Kerala")
        put("tamil nadu", "Tamil Nadu")
        put("karnataka", "Karnataka")
        put("maharashtra", "Maharashtra")

        // ─── More Adjectives ───
        put("lamba", "tall")
        put("lambi", "tall")
        put("chota", "small")
        put("choti", "small")
        put("bada", "big")
        put("badi", "big")
        put("bade", "big")
        put("mota", "fat")
        put("patla", "thin")
        put("patli", "thin")
        put("gora", "fair")
        put("kala", "dark")
        put("naya", "new")
        put("nayi", "new")
        put("purana", "old")
        put("purani", "old")
        put("jawan", "young")
        put("budha", "old aged")
        put("budhi", "old aged")
        put("amir", "rich")
        put("gharib", "poor")
        put("garib", "poor")
        put("hoshiyar", "clever")
        put("bewakoof", "foolish")
        put("seedha", "simple")
        put("sidha", "straight")
        put("tedha", "crooked")
        put("gehra", "deep")
        put("gehri", "deep")
        put("chauda", "wide")
        put("chaudi", "wide")
        put("tang", "narrow")
        put("uuncha", "high")
        put("uunchi", "high")
        put("neecha", "low")
        put("neechi", "low")
        put("garam", "hot")
        put("thanda", "cold")
        put("naram", "soft")
        put("sakht", "hard")
        put("mulaayam", "soft")
        put("kadak", "harsh")
        put("mitha", "sweet")
        put("mithi", "sweet")
        put("khatta", "sour")
        put("khatti", "sour")
        put("teekha", "spicy")
        put("namkeen", "salty")
        put("kadva", "bitter")
        put("swad", "taste")
        put("swadisht", "tasty")
        put("be swad", "tasteless")
        put("fresh", "fresh")
        put("taza", "fresh")

        // ─── More Verbs ───
        put("aana", "come")
        put("jaana", "go")
        put("karna", "do")
        put("khana", "eat")
        put("peena", "drink")
        put("padhna", "study")
        put("likhna", "write")
        put("bolna", "speak")
        put("kehna", "say")
        put("samajhna", "understand")
        put("dekhna", "see")
        put("sunna", "listen")
        put("sona", "sleep")
        put("uthna", "wake")
        put("baithna", "sit")
        put("khade hona", "stand")
        put("chalna", "walk")
        put("daudna", "run")
        put("tairna", "swim")
        put("khelna", "play")
        put("nachna", "dance")
        put("gana", "sing")
        put("pakana", "cook")
        put("pakaana", "cook")
        put("dhona", "wash")
        put("saf kar", "clean")
        put("saf karna", "clean")
        put("jhadoo", "sweep")
        put("ponch", "wipe")
        put("ranna", "cook")
        put("hilna", "move")
        put("rukna", "stop")
        put("mudna", "turn")
        put("ghoomna", "roam")
        put("milna", "meet")
        put("bhejna", "send")
        put("lena", "take")
        put("dena", "give")
        put("rakhna", "keep")
        put("laana", "bring")
        put("bulaana", "call")
        put("pukarna", "call")
        put("puchhna", "ask")
        put("puchna", "ask")
        put("batana", "tell")
        put("dikhaana", "show")
        put("dikhana", "show")
        put("samjhana", "explain")
        put("samjhaana", "explain")
        put("sikhaana", "teach")
        put("sikhna", "learn")
        put("khareedna", "buy")
        put("kharidna", "buy")
        put("bechna", "sell")
        put("khona", "lose")
        put("dhudna", "search")
        put("dhundhna", "search")
        put("paana", "get")
        put("rokna", "stop")
        put("todna", "break")
        put("jodna", "join")
        put("kholna", "open")
        put("band karna", "close")
        put("chhupana", "hide")
        put("bhagna", "run away")
        put("ladna", "fight")
        put("jhoot bolna", "lie")
        put("sach bolna", "tell truth")
        put("waada karna", "promise")
        put("kasam khana", "swear")
        put("maan lena", "accept")
        put("inkar karna", "refuse")
        put("mana karna", "forbid")
        put("chunna", "choose")
        put("select karna", "select")
        put("try karna", "try")
        put("koshish karna", "attempt")

        // ─── Body Parts ───
        put("sar", "head")
        put("sir", "head")
        put("baal", "hair")
        put("aankh", "eye")
        put("aankhen", "eyes")
        put("naak", "nose")
        put("kann", "ear")
        put("kaan", "ear")
        put("kane", "ears")
        put("muh", "mouth")
        put("munh", "mouth")
        put("hoth", "lip")
        put("hoth", "lips")
        put("jubaan", "tongue")
        put("daant", "teeth")
        put("gardan", "neck")
        put("kandha", "shoulder")
        put("kandhe", "shoulders")
        put("baazoo", "arm")
        put("baahe", "arms")
        put("haath", "hand")
        put("hath", "hand")
        put("hathon", "hands")
        put("ungli", "finger")
        put("ungliyan", "fingers")
        put("nakhun", "nail")
        put("chhaati", "chest")
        put("pet", "stomach")
        put("pith", "back")
        put("kamar", "waist")
        put("tung", "leg")
        put("tang", "leg")
        put("pair", "foot")
        put("pairo", "feet")
        put("ghutna", "knee")
        put("ghutne", "knees")
        put("edhi", "heel")

        // ─── Food Items ───
        put("chawal", "rice")
        put("roti", "bread")
        put("dal", "lentils")
        put("sabzi", "vegetables")
        put("salad", "salad")
        put("dahi", "yogurt")
        put("doodh", "milk")
        put("chai", "tea")
        put("coffee", "coffee")
        put("juice", "juice")
        put("paani", "water")
        put("namak", "salt")
        put("chini", "sugar")
        put("shakkar", "sugar")
        put("mirchi", "chili")
        put("haldi", "turmeric")
        put("dhaniya", "coriander")
        put("jeera", "cumin")
        put("elaichi", "cardamom")
        put("daalchini", "cinnamon")
        put("laung", "clove")
        put("kali mirch", "pepper")
        put("sookhi mirch", "dry chili")
        put("aata", "flour")
        put("maida", "white flour")
        put("sooji", "semolina")
        put("tel", "oil")
        put("ghee", "ghee")
        put("makkhan", "butter")
        put("paneer", "paneer")
        put("anda", "egg")
        put("murgi", "chicken")
        put("machhli", "fish")
        put("gosht", "meat")
        put("kela", "banana")
        put("seb", "apple")
        put("santra", "orange")
        put("amrood", "guava")
        put("angoor", "grapes")
        put("aam", "mango")
        put("papita", "papaya")
        put("tarbooj", "watermelon")
        put("nariyal", "coconut")
        put("mungfali", "peanut")
        put("badam", "almond")
        put("akharot", "walnut")
        put("kachha", "raw")
        put("pakka", "ripe")
        put("gehu", "wheat")
        put("jau", "barley")

        // ─── Ability Modals ───
        put("sakta", "can")
        put("sakti", "can")
        put("sakte", "can")
        put("sakta hai", "can")
        put("sakti hai", "can")
        put("sakte hain", "can")
        put("sakta hoon", "can")

        // ─── Desire / Need ───
        put("chahiye", "want")
        put("chahta", "want")
        put("chahti", "want")
        put("chahte", "want")
        put("chahta hoon", "I want")
        put("chahti hoon", "I want")
        put("chahte hain", "we want")
        put("mujhe chahiye", "I want")
        put("mujhe", "I")
        put("mujhko", "me")

        // ─── Past Tense: hua/hui/huye (happen) ───
        put("hua", "happened")
        put("hui", "happened")
        put("huye", "happened")
        put("ho", "happen")
        put("hota", "happens")
        put("hoti", "happens")
        put("hote", "happen")

        // ─── Past Tense: mila/mili/mile (got/met) ───
        put("mila", "got")
        put("mili", "got")
        put("mile", "got")
        put("mil", "get")
        put("milna", "get")
        put("milo", "meet")
        put("milte", "meet")

        // ─── Past Tense: socha (thought) ───
        put("socha", "thought")
        put("sochi", "thought")
        put("soche", "thought")
        put("soch", "think")
        put("socho", "think")
        put("sochte", "think")

        // ─── Past Tense: rakha (kept) ───
        put("rakha", "kept")
        put("rakhi", "kept")
        put("rakhe", "kept")
        put("rakh", "keep")

        // ─── Past Tense: padha (studied/read) ───
        put("padha", "studied")
        put("padhi", "studied")
        put("padhe", "studied")
        put("padh", "study")
        put("padho", "study")
        put("padhte", "study")
        put("padhti", "studies")

        // ─── Past Tense: likha (wrote) ───
        put("likha", "wrote")
        put("likhi", "wrote")
        put("likhe", "wrote")
        put("likh", "write")
        put("likho", "write")
        put("likhte", "write")
        put("likhti", "writes")

        // ─── Past Tense: utha (woke/got up) ───
        put("utha", "woke up")
        put("uthi", "woke up")
        put("uthe", "woke up")
        put("uth", "wake up")
        put("utho", "wake up")

        // ─── Past Tense: baitha (sat) ───
        put("baitha", "sat")
        put("baithi", "sat")
        put("baithe", "sat")
        put("baith", "sit")
        put("baitho", "sit")
        put("baithna", "sit")
        put("baithke", "sitting")

        // ─── Past Tense: khada (stood) ───
        put("khada", "stood")
        put("khadi", "stood")
        put("khade", "stood")
        put("kharha", "stood")
        put("kharhi", "stood")
        put("kharhe", "stood")
        put("kharha hona", "stand")
        put("khade hona", "stand")

        // ─── Past Tense: chala (walked/left) ───
        put("chala", "left")
        put("chali", "left")
        put("chale", "left")
        put("chal", "walk")
        put("chalo", "let's go")
        put("chalte", "walk")
        put("chalti", "walks")

        // ─── Past Tense: soya (slept) ───
        put("soya", "slept")
        put("soyi", "slept")
        put("soye", "slept")
        put("so", "sleep")
        put("soo", "sleep")
        put("sona", "sleep")
        put("sota", "sleeps")
        put("soti", "sleeps")
        put("sote", "sleep")

        // ─── Past Tense: roya (cried) ───
        put("roya", "cried")
        put("royi", "cried")
        put("roye", "cried")
        put("ro", "cry")
        put("rona", "cry")
        put("rota", "cries")

        // ─── Past Tense: hasa (laughed) ───
        put("hasa", "laughed")
        put("hasi", "laughed")
        put("hase", "laughed")
        put("has", "laugh")
        put("haso", "laugh")
        put("hans", "laugh")
        put("hansna", "laugh")
        put("hasta", "laughs")

        // ─── Past Tense: gaya/gayi/gaye (went) - additional forms ───
        put("gaya", "went")
        put("gayi", "went")
        put("gaye", "went")
        put("ja", "go")
        put("jaa", "go")
        put("jao", "go")
        put("jata", "goes")
        put("jati", "goes")
        put("jate", "go")

        // ─── Past Tense: aaya (came) - additional forms ───
        put("aaya", "came")
        put("aayi", "came")
        put("aaye", "came")
        put("aa", "come")
        put("aao", "come")

        // ─── Emotions ───
        put("khushi", "happy")
        put("khush", "happy")
        put("dukhi", "sad")
        put("dukh", "sadness")
        put("gussa", "angry")
        put("gusse", "anger")
        put("naraz", "upset")
        put("pareshaan", "worried")
        put("tension", "stress")
        put("pyaar", "love")
        put("mohabbat", "love")
        put("nafrat", "hate")
        put("dar", "fear")
        put("dara", "scared")
        put("dari", "scared")
        put("dare", "scared")
        put("dar lagta", "afraid")

        // ─── State / Health ───
        put("thak gaya", "tired")
        put("thak gayi", "tired")
        put("thak gaye", "tired")
        put("thak", "tire")
        put("thakna", "tire")
        put("thaka", "tired")
        put("thaki", "tired")
        put("thake", "tired")
        put("bimar", "sick")
        put("bemaar", "sick")
        put("bimaar", "sick")
        put("swasth", "healthy")
        put("tandurust", "healthy")
        put("thik", "fine")
        put("theek", "fine")
        put("achha", "good")
        put("behtar", "better")
        put("kharab", "bad")
        put("buri tarah", "badly")
        put("aaram", "rest")
        put("aaram karna", "rest")

        // ─── Perception / Cognition ───
        put("yaad", "remember")
        put("yaad hai", "remember")
        put("yaad aaya", "remembered")
        put("bhool", "forget")
        put("bhul", "forget")
        put("bhool gaya", "forgot")
        put("bhool gayi", "forgot")
        put("samajh", "understand")
        put("samajh aaya", "understood")
        put("samajh mein aaya", "understood")
        put("pata", "know")
        put("pata hai", "know")
        put("pata nahi", "do not know")
        put("malum", "known")
        put("malum hai", "know")

        // ─── Communication ───
        put("khabar", "news")
        put("awaaz", "voice")
        put("awaz", "voice")
        put("sawaal", "question")
        put("jawaab", "answer")
        put("javaab", "answer")
        put("baat", "talk")
        put("baatein", "talks")
        put("baatcheet", "conversation")
        put("shor", "noise")
        put("khamoshi", "silence")
        put("chup", "quiet")

        // ─── Time ───
        put("shuru", "start")
        put("suru", "start")
        put("khatam", "finish")
        put("khtam", "finish")
        put("samay", "time")
        put("waqt", "time")
        put("vela", "time")
        put("pala", "moment")
        put("lamha", "moment")
        put("waada", "promise")
        put("vaada", "promise")

        // ─── Quality / Quantity ───
        put("aasan", "easy")
        put("mushkil", "difficult")
        put("kathin", "hard")
        put("safal", "successful")
        put("kaamyab", "successful")
        put("nakam", "failed")
        put("naakaam", "failed")
        put("fayda", "benefit")
        put("fayeda", "benefit")
        put("nuksan", "loss")
        put("koshish", "attempt")
        put("koshish karna", "try")
        put("himmat", "courage")
        put("sahas", "bravery")
        put("shakti", "strength")
        put("taakat", "power")
        put("takleef", "trouble")
        put("mushkil", "difficulty")

        // ─── Nature / Environment ───
        put("andhera", "darkness")
        put("ujala", "light")
        put("ujala", "brightness")
        put("roshni", "light")
        put("chand", "moon")
        put("chandni", "moonlight")
        put("suraj", "sun")
        put("taare", "stars")
        put("aasmaan", "sky")
        put("zameen", "ground")
        put("hawaa", "air")
        put("hawa", "air")
        put("aag", "fire")
        put("aankh", "eye")
    }

    private val knownVerbs = setOf(
        "eat", "drink", "go", "come", "do", "make", "take", "give", "get", "see",
        "tell", "show", "ask", "help", "like", "love", "want", "need", "know",
        "think", "say", "put", "bring", "buy", "sell", "send", "receive", "find",
        "keep", "hold", "carry", "pull", "push", "open", "close", "start", "stop",
        "finish", "work", "play", "read", "write", "speak", "talk", "listen",
        "hear", "watch", "look", "run", "walk", "sit", "stand", "sleep", "wake",
        "wash", "clean", "cook", "cut", "break", "fix", "build", "grow", "change",
        "move", "turn", "call", "answer", "meet", "visit", "wait", "try", "pay",
        "use", "live", "die", "win", "lose", "fight", "sing", "dance", "draw",
        "paint", "teach", "learn", "study", "understand", "remember", "forget",
        "believe", "hope", "wish", "share", "save", "spend", "waste", "miss",
        "catch", "throw", "kick", "hit", "pick", "choose", "decide", "plan",
        "allow", "forbid", "invite", "join", "leave", "arrive", "return",
        "order", "serve", "prepare", "boil", "fry", "bake", "roast", "grill",
        "steam", "stir", "mix", "add", "put", "keep", "said", "gave", "took",
        "ate", "drank", "went", "came", "did", "made", "told", "brought",
        "bought", "sold", "sent", "found", "taught", "learnt", "meant",
        "built", "spent", "lost", "won", "drove", "rode", "swam", "sang",
        "threw", "caught", "slept", "woke", "wore", "chose", "spoke",
        "broke", "froze", "grew", "hid", "bit", "blew", "drew", "flew",
        "forgot", "forgave", "hung", "led", "paid", "quit", "rang", "rose",
        "shook", "shone", "shut", "sank", "slid", "stole", "stuck", "swore",
        "swept", "swung", "tore", "understood", "wound", "wrote",
    )

    private val subjects = setOf("I", "You", "He", "She", "We", "They", "It")

    private fun reorderToEnglish(text: String): String {
        var result = text

        val removals = setOf("raha", "rahi", "rahe", "hoon", "ho", "hain", "hai", "tha", "the", "thi", "thay", "hoga", "hogee", "honge", "kar", "karke", "ke", "kar ke", "kr", "wala", "wali", "wale", "ne", "se", "mein", "ko", "ka", "ki")
        result = result.split(" ").filter { it.lowercase() !in removals }.joinToString(" ")

        val words = result.split(" ").toMutableList()
        if (words.size <= 2) return words.joinToString(" ")

        val firstCap = words[0].replaceFirstChar { it.uppercase() }
        val isSubject = firstCap in subjects
        if (!isSubject) return result

        val beVerbs = setOf("am", "is", "are", "was", "were", "been", "being")

        val ingIdx = words.indexOfFirst { it.endsWith("ing") }
        val beIdx = words.indexOfFirst { it in beVerbs }

        if (ingIdx > 1 && beIdx >= 0) {
            val verb = words.removeAt(ingIdx)
            val insertAt = beIdx + 1
            if (insertAt <= words.size) words.add(insertAt, verb) else words.add(verb)
            return words.joinToString(" ")
        }

        val lastVerbIdx = words.indices.lastOrNull { words[it].lowercase() in knownVerbs }
        if (lastVerbIdx != null && lastVerbIdx == words.size - 1 && lastVerbIdx > 1) {
            val verb = words.removeAt(lastVerbIdx)
            words.add(1, verb)
        }

        return words.joinToString(" ")
    }
}
