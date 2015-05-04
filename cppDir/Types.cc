
class Float;

class Int {

private:

	friend class Float;
	int data;

public:

	void operator = (const Int &rhs);
	Int (int initIng);
	Int ();
	const Float operator + (const Float &rhs) const;
	const Int operator + (const Int &rhs) const;
	const Float operator - (const Float &rhs) const;
	const Int operator - (const Int &rhs) const;
	const Float operator / (const Float &rhs) const;
	const Int operator / (const Int &rhs) const;
	const Float operator * (const Float &rhs) const;
	const Int operator * (const Int &rhs) const;
	bool operator < (const Float &rhs) const;
	bool operator < (const Int &rhs) const;
	bool operator > (const Float &rhs) const;
	bool operator > (const Int &rhs) const;
	bool operator == (const Float &rhs) const;
	bool operator == (const Int &rhs) const;
	bool ReadIn (FILE * fromMe);
	void WriteOut (FILE * toMe) const;
	long GetHash ();
};

class Float {


private:

	friend class Int;
	double data;

public:

	void operator = (const Float &rhs);
	void operator = (const Int &rhs);
	Float (double initDouble);
	Float ();
	const Float operator + (const Float &rhs) const;
	const Float operator + (const Int &rhs) const;
	const Float operator - (const Float &rhs) const;
	const Float operator - (const Int &rhs) const;
	const Float operator / (const Float &rhs) const;
	const Float operator / (const Int &rhs) const;
	const Float operator * (const Float &rhs) const;
	const Float operator * (const Int &rhs) const;
	bool operator < (const Float &rhs) const;
	bool operator < (const Int &rhs) const;
	bool operator > (const Float &rhs) const;
	bool operator > (const Int &rhs) const;
	bool operator == (const Float &rhs) const;
	bool operator == (const Int &rhs) const;
	bool ReadIn (FILE * fromMe);
	void WriteOut (FILE * toMe) const;
	long GetHash ();
};

class Str {

private:

	char *data;

public:

	void operator = (const Str &rhs);
	Str (char *initString);
	Str ();
	~Str ();
	const Str operator + (const Str &rhs) const;
	bool operator < (const Str &rhs) const;
	bool operator > (const Str &rhs) const;
	bool operator == (const Str &rhs) const;
	bool ReadIn (FILE * fromMe);
	void WriteOut (FILE * toMe) const;
	long GetHash ();
};

long Float :: GetHash () {

	long returnVal = 0;
	char temp[sizeof (double) + 6];
	for (int i = 0; i < sizeof (double) + 6; i++) {
		temp[i] = 0;
	}
	memmove (temp, &data, sizeof (double));
	for (int i = 0; i < sizeof (double); i+= 6) {
		seed48 ((unsigned short *) (temp + i));
		returnVal = returnVal ^ lrand48 ();	
	}
	return returnVal;
}

long Int :: GetHash () {
	srand48 (data);
	long returnVal = lrand48 ();
	return returnVal;	
}

long Str :: GetHash () {
	long returnVal = 0;
	char temp[strlen (data) + 6];
	for (int i = 0; i < strlen (data) + 6; i++) {
		temp[i] = 0;
	}
	for (int i = 0; i < strlen (data); i++) {
		temp[i] = tolower (data[i]);
	}
	for (int i = 0; i < strlen (data); i+= 6) {
		seed48 ((unsigned short *) (temp + i));
		returnVal = (returnVal ^ lrand48 ());	
	}
	return returnVal;
}

void Float :: operator = (const Float &rhs) {
	data = rhs.data;
}

void Float :: operator = (const Int &rhs) {
	data = rhs.data;
}

Float :: Float (double initDouble) {
	data = initDouble;
}

Float :: Float () {
	data = 0.0;
}

const Float Float :: operator + (const Float &rhs) const {
	return Float (data + rhs.data);	
}

const Float Float :: operator + (const Int &rhs) const {
	return Float (data + rhs.data);	
}

const Float Float :: operator - (const Float &rhs) const {
	return Float (data - rhs.data);	
}

const Float Float :: operator - (const Int &rhs) const {
	return Float (data - rhs.data);	
}

const Float Float :: operator / (const Float &rhs) const {
	return Float (data / rhs.data);	
}

const Float Float :: operator / (const Int &rhs) const {
	return Float (data / rhs.data);	
}

const Float Float :: operator * (const Float &rhs) const {
	return Float (data * rhs.data);	
}

const Float Float :: operator * (const Int &rhs) const {
	return Float (data * rhs.data);	
}

bool Float :: operator < (const Float &rhs) const {
	return data < rhs.data;
}

bool Float :: operator < (const Int &rhs) const {
	return data < rhs.data;
}

bool Float :: operator > (const Float &rhs) const {
	return data > rhs.data;
}

bool Float :: operator > (const Int &rhs) const {
	return data > rhs.data;
}

bool Float :: operator == (const Float &rhs) const {
	return data > rhs.data;
}

bool Float :: operator == (const Int &rhs) const {
	return data > rhs.data;
}

void Str :: operator = (const Str &rhs) {
	if (data != 0) {
		free (data);
	}
	data = strdup (rhs.data);
}
	
Str :: Str (char *initString) {
	data = strdup (initString);	
}
	
Str :: Str () {
	data = 0;
}
	
Str :: ~Str () {
	if (data != 0) {
		free (data);
		data = 0;
	}
}
	
const Str Str :: operator + (const Str &rhs) const {
	static char buffer[500];
	sprintf (buffer, "%s%s", data, rhs.data);
	return Str (buffer);
}

bool Str :: operator < (const Str &rhs) const {
	return strcasecmp (data, rhs.data) < 0;
}

bool Str :: operator > (const Str &rhs) const {
	return strcasecmp (data, rhs.data) > 0;
}
	
bool Str :: operator == (const Str &rhs) const {
	return !strcasecmp (data, rhs.data);
}

void Int :: operator = (const Int &rhs) {
	data = rhs.data;
}

Int :: Int (int initInt) {
	data = initInt;
}

Int :: Int () {
	data = 0;
}

const Float Int :: operator + (const Float &rhs) const {
	return Float (data + rhs.data);
}

const Int Int :: operator + (const Int &rhs) const {
	return Int (data + rhs.data);
}

const Float Int :: operator - (const Float &rhs) const {
	return Float (data - rhs.data);
}

const Int Int :: operator - (const Int &rhs) const {
	return Int (data - rhs.data);
}

const Float Int :: operator / (const Float &rhs) const {
	return Float (data / rhs.data);
}

const Int Int :: operator / (const Int &rhs) const {
	return Int (data / rhs.data);
}

const Float Int :: operator * (const Float &rhs) const {
	return Float (data * rhs.data);
}

const Int Int :: operator * (const Int &rhs) const {
	return Int (data * rhs.data);
}

bool Int :: operator < (const Float &rhs) const {
	return data < rhs.data;
}

bool Int :: operator < (const Int &rhs) const {
	return data < rhs.data;
}

bool Int :: operator > (const Float &rhs) const {
	return data > rhs.data;
}

bool Int :: operator > (const Int &rhs) const {
	return data > rhs.data;
}

bool Int :: operator == (const Float &rhs) const {
	return data == rhs.data;
}

bool Int :: operator == (const Int &rhs) const {
	return data == rhs.data;
}

bool Int :: ReadIn (FILE * fromMe) {
	static char buffer[500];
	int pos = 0;
	while ((buffer[pos++] = fgetc (fromMe)) != '|') {
		if (buffer[pos - 1] == EOF)
			return false;
	}
	buffer[pos - 1] = 0;
	data = atoi (buffer);
	return true;
}

void Int :: WriteOut (FILE * toMe) const {
	fprintf (toMe, "%d|", data);
}

bool Float :: ReadIn (FILE * fromMe) {
	static char buffer[500];
	int pos = 0;
	while ((buffer[pos++] = fgetc (fromMe)) != '|') {
		if (buffer[pos - 1] == EOF)
			return false;
	}
	buffer[pos - 1] = 0;
	data = atof (buffer);
	return true;
}

void Float :: WriteOut (FILE * toMe) const {
	fprintf (toMe, "%lf|", data);
}

bool Str :: ReadIn (FILE * fromMe) {
	static char buffer[500];
	int pos = 0;
	while ((buffer[pos++] = fgetc (fromMe)) != '|') {
		if (buffer[pos - 1] == EOF)
			return false;
	}
	buffer[pos - 1] = 0;
	if (data != 0)
		free (data);
	data = strdup (buffer);
	return true;
}

void Str :: WriteOut (FILE * toMe) const {
	fprintf (toMe, "%s|", data);
}

