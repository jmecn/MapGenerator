#include <iostream>
#include <string>
#include <random>
#include <cassert>
 
enum class Tile
{
	Unused,
	DirtWall,
	DirtFloor,
	Corridor,
	Door,
	UpStairs,
	DownStairs
};
 
enum class Direction
{
	North, South, East, West,
};
 
class Map
{
public:
 
	Map():
		xSize(0), ySize(0),
		data() { }
 
	Map(int x, int y, Tile value = Tile::Unused):
		xSize(x), ySize(y),
		data(x * y, value) { }
 
	void SetCell(int x, int y, Tile celltype)
	{
		assert(IsXInBounds(x));
		assert(IsYInBounds(y));
 
		data[x + xSize * y] = celltype;
	}
 
	Tile GetCell(int x, int y) const
	{
		assert(IsXInBounds(x));
		assert(IsYInBounds(y));
 
		return data[x + xSize * y];
	}
 
	void SetCells(int xStart, int yStart, int xEnd, int yEnd, Tile cellType)
	{
		assert(IsXInBounds(xStart) && IsXInBounds(xEnd));
		assert(IsYInBounds(yStart) && IsYInBounds(yEnd));
 
		assert(xStart <= xEnd);
		assert(yStart <= yEnd);
 
		for (auto y = yStart; y != yEnd + 1; ++y)
			for (auto x = xStart; x != xEnd + 1; ++x)
				SetCell(x, y, cellType);
	}
 
	bool IsXInBounds(int x) const
	{
		return x >= 0 && x < xSize;
	}
 
	bool IsYInBounds(int y) const
	{
		return y >= 0 && y < ySize;
	}
 
	bool IsAreaUnused(int xStart, int yStart, int xEnd, int yEnd)
	{
		assert(IsXInBounds(xStart) && IsXInBounds(xEnd));
		assert(IsYInBounds(yStart) && IsYInBounds(yEnd));
 
		assert(xStart <= xEnd);
		assert(yStart <= yEnd);
 
		for (auto y = yStart; y != yEnd + 1; ++y)
			for (auto x = xStart; x != xEnd + 1; ++x)
				if (GetCell(x, y) != Tile::Unused)
					return false;
 
		return true;
	}
 
	bool IsAdjacent(int x, int y, Tile tile)
	{
		assert(IsXInBounds(x - 1) && IsXInBounds(x + 1));
		assert(IsYInBounds(y - 1) && IsYInBounds(y + 1));
 
		return 
			GetCell(x - 1, y) == tile || GetCell(x + 1, y) == tile ||
			GetCell(x, y - 1) == tile || GetCell(x, y + 1) == tile;
	}
 
	void Print() const
	{
		// TODO: proper ostream iterator.
		// TODO: proper lookup of character from enum.
 
		for (auto y = 0; y != ySize; y++)
		{
			for (auto x = 0; x != xSize; x++)
			{
				switch(GetCell(x, y))
				{
				case Tile::Unused:
					std::cout << " ";
					break;
				case Tile::DirtWall:
					std::cout << "#";
					break;
				case Tile::DirtFloor:
					std::cout << ".";
					break;
				case Tile::Corridor:
					std::cout << ".";
					break;
				case Tile::Door:
					std::cout << "+";
					break;
				case Tile::UpStairs:
					std::cout << "<";
					break;
				case Tile::DownStairs:
					std::cout << ">";
					break;
				};
			}
 
			std::cout << std::endl;
		}
 
		std::cout << std::endl;
	}
 
private:
 
	int xSize, ySize;
 
	std::vector<Tile> data;
};
 
class DungeonGenerator
{
public:
 
	int Seed;
 
	int XSize, YSize;
 
	int MaxFeatures;
 
	int ChanceRoom, ChanceCorridor;
 
	DungeonGenerator():
		Seed(std::random_device()()),
		XSize(80), YSize(25),
		MaxFeatures(100),
		ChanceRoom(75), ChanceCorridor(25) { }
 
	Map Generate()
	{
		// TODO: proper input validation.
		assert(MaxFeatures > 0 && MaxFeatures <= 100);
		assert(XSize > 3 && XSize <= 80);
		assert(YSize > 3 && YSize <= 25);
 
		auto rng = RngT(Seed);
		auto map = Map(XSize, YSize, Tile::Unused);
 
		MakeDungeon(map, rng);
 
		return map;
	}
 
private:
 
	typedef std::mt19937 RngT;
 
	int GetRandomInt(RngT& rng, int min, int max) const
	{
		return std::uniform_int_distribution<int>(min, max)(rng);
	}
 
	Direction GetRandomDirection(RngT& rng) const
	{
		return Direction(std::uniform_int_distribution<int>(0, 3)(rng));
	}
 
	bool MakeCorridor(Map& map, RngT& rng, int x, int y, int maxLength, Direction direction) const
	{
		assert(x >= 0 && x < XSize);
		assert(y >= 0 && y < YSize);
 
		assert(maxLength > 0 && maxLength <= std::max(XSize, YSize));
 
		auto length = GetRandomInt(rng, 2, maxLength);
 
		auto xStart = x;
		auto yStart = y;
 
		auto xEnd = x;
		auto yEnd = y;
 
		if (direction == Direction::North)
			yStart = y - length;
		else if (direction == Direction::East)
			xEnd = x + length;
		else if (direction == Direction::South)
			yEnd = y + length;
		else if (direction == Direction::West)
			xStart = x - length;
 
		if (!map.IsXInBounds(xStart) || !map.IsXInBounds(xEnd) || !map.IsYInBounds(yStart) || !map.IsYInBounds(yEnd))
			return false;
 
		if (!map.IsAreaUnused(xStart, yStart, xEnd, yEnd))
			return false;
 
		map.SetCells(xStart, yStart, xEnd, yEnd, Tile::Corridor);
 
		//std::cout << "Corridor: ( " << xStart << ", " << yStart << " ) to ( " << xEnd << ", " << yEnd << " )" << std::endl;
 
		return true;
	}
 
	bool MakeRoom(Map& map, RngT& rng, int x, int y, int xMaxLength, int yMaxLength, Direction direction) const
	{
		// Minimum room size of 4x4 tiles (2x2 for walking on, the rest is walls)
		auto xLength = GetRandomInt(rng, 4, xMaxLength);
		auto yLength = GetRandomInt(rng, 4, yMaxLength);
 
		auto xStart = x;
		auto yStart = y;
 
		auto xEnd = x;
		auto yEnd = y;
 
		if (direction == Direction::North)
		{
			yStart = y - yLength;
			xStart = x - xLength / 2;
			xEnd = x + (xLength + 1) / 2;
		}
		else if (direction == Direction::East)
		{
			yStart = y - yLength / 2;
			yEnd = y + (yLength + 1) / 2;
			xEnd = x + xLength;
		}
		else if (direction == Direction::South)
		{
			yEnd = y + yLength;
			xStart = x - xLength / 2;
			xEnd = x + (xLength + 1) / 2;
		}
		else if (direction == Direction::West)
		{
			yStart = y - yLength / 2;
			yEnd = y + (yLength + 1) / 2;
			xStart = x - xLength;
		}
 
		if (!map.IsXInBounds(xStart) || !map.IsXInBounds(xEnd) || !map.IsYInBounds(yStart) || !map.IsYInBounds(yEnd))
			return false;
 
		if (!map.IsAreaUnused(xStart, yStart, xEnd, yEnd))
			return false;
 
		map.SetCells(xStart, yStart, xEnd, yEnd, Tile::DirtWall);
		map.SetCells(xStart + 1, yStart + 1, xEnd - 1, yEnd - 1, Tile::DirtFloor);
 
		//std::cout << "Room: ( " << xStart << ", " << yStart << " ) to ( " << xEnd << ", " << yEnd << " )" << std::endl;
 
		return true;
	}
 
	bool MakeFeature(Map& map, RngT& rng, int x, int y, int xmod, int ymod, Direction direction) const
	{
		// Choose what to build
		auto chance = GetRandomInt(rng, 0, 100);
 
		if (chance <= ChanceRoom)
		{
			if (MakeRoom(map, rng, x + xmod, y + ymod, 8, 6, direction))
			{
				map.SetCell(x, y, Tile::Door);
 
				// Remove wall next to the door.
				map.SetCell(x + xmod, y + ymod, Tile::DirtFloor);
 
				return true;
			}
 
			return false;
		}
		else
		{
			if (MakeCorridor(map, rng, x + xmod, y + ymod, 6, direction))
			{
				map.SetCell(x, y, Tile::Door);
 
				return true;
			}
 
			return false;
		}
	}
 
	bool MakeFeature(Map& map, RngT& rng) const
	{
		auto tries = 0;
		auto maxTries = 1000;
 
		for( ; tries != maxTries; ++tries)
		{
			// Pick a random wall or corridor tile.
			// Make sure it has no adjacent doors (looks weird to have doors next to each other).
			// Find a direction from which it's reachable.
			// Attempt to make a feature (room or corridor) starting at this point.
 
			int x = GetRandomInt(rng, 1, XSize - 2);
			int y = GetRandomInt(rng, 1, YSize - 2);
 
			if (map.GetCell(x, y) != Tile::DirtWall && map.GetCell(x, y) != Tile::Corridor)
				continue;
 
			if (map.IsAdjacent(x, y, Tile::Door))
				continue;
 
			if (map.GetCell(x, y+1) == Tile::DirtFloor || map.GetCell(x, y+1) == Tile::Corridor)
			{
				if (MakeFeature(map, rng, x, y, 0, -1, Direction::North))
					return true;
			}
			else if (map.GetCell(x-1, y) == Tile::DirtFloor || map.GetCell(x-1, y) == Tile::Corridor)
			{
				if (MakeFeature(map, rng, x, y, 1, 0, Direction::East))
					return true;
			}
			else if (map.GetCell(x, y-1) == Tile::DirtFloor || map.GetCell(x, y-1) == Tile::Corridor)
			{
				if (MakeFeature(map, rng, x, y, 0, 1, Direction::South))
					return true;
			}
			else if (map.GetCell(x+1, y) == Tile::DirtFloor || map.GetCell(x+1, y) == Tile::Corridor)
			{
				if (MakeFeature(map, rng, x, y, -1, 0, Direction::West))
					return true;
			}
		}
 
		return false;
	}
 
	bool MakeStairs(Map& map, RngT& rng, Tile tile) const
	{
		auto tries = 0;
		auto maxTries = 10000;
 
		for ( ; tries != maxTries; ++tries)
		{
			int x = GetRandomInt(rng, 1, XSize - 2);
			int y = GetRandomInt(rng, 1, YSize - 2);
 
			if (!map.IsAdjacent(x, y, Tile::DirtFloor) && !map.IsAdjacent(x, y, Tile::Corridor))
				continue;
 
			if (map.IsAdjacent(x, y, Tile::Door))
				continue;
 
			map.SetCell(x, y, tile);
 
			return true;
		}
 
		return false;
	}
 
	bool MakeDungeon(Map& map, RngT& rng) const
	{
		// Make one room in the middle to start things off.
		MakeRoom(map, rng, XSize / 2, YSize / 2, 8, 6, GetRandomDirection(rng));
 
		for (auto features = 1; features != MaxFeatures; ++features)
		{
			if (!MakeFeature(map, rng))
			{
				std::cout << "Unable to place more features (placed " << features << ")." << std::endl;
				break;
			}
		}
 
		if (!MakeStairs(map, rng, Tile::UpStairs))
			std::cout << "Unable to place up stairs." << std::endl;
 
		if (!MakeStairs(map, rng, Tile::DownStairs))
			std::cout << "Unable to place down stairs." << std::endl;
 
		return true;
	}
 
};
 
int main()
{
	DungeonGenerator generator;
 
	auto map = generator.Generate();
 
	map.Print();
}