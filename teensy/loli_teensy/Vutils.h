#ifndef _VUTILS_h
#define _VUTILS_h

#include <vector>

template<size_t size>
class Vutils
{
public:
	Vutils(){}

	static int32_t vtoi(std::vector<uint8_t> const & v)
	{
		char str[size];
		vtostr(v, str);
		return strtol(str, NULL, 10);
	}

	static float vtof(std::vector<uint8_t> const & v)
	{
		char str[size];
		vtostr(v, str);
		return strtof(str, NULL);
	}

	static void vtostr(std::vector<uint8_t> const & v, char str[size])
	{
		if (v.size() < size)
		{
			for (size_t i = 0; i < v.size(); i++)
			{
				str[i] = v.at(i);
			}
			str[v.size()] = '\0';
		}
		else
		{
			str[0] = '\0';
		}
	}
};


#endif

