'use client';

import { User } from '@/types';
import { Users, Circle } from 'lucide-react';

interface UserListProps {
  users: User[];
  currentUserEmail?: string;
}

export function UserList({ users, currentUserEmail }: UserListProps) {
  return (
    <div className="w-80 border-l border-gray-200 bg-gray-50 flex flex-col">
      <div className="p-4 border-b border-gray-200 bg-white">
        <div className="flex items-center gap-2">
          <Users className="w-5 h-5 text-gray-600" />
          <h2 className="font-semibold text-gray-900">
            Online Users ({users.length})
          </h2>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {users.length === 0 ? (
          <div className="p-4 text-center text-gray-500">
            No users online
          </div>
        ) : (
          <div className="p-2">
            {users.map((user) => (
              <div
                key={user.email}
                className={`flex items-center gap-3 p-3 rounded-lg mb-1 ${
                  user.email === currentUserEmail
                    ? 'bg-blue-50 border border-blue-200'
                    : 'hover:bg-gray-100'
                } transition-colors`}
              >
                <div className="relative">
                  <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white font-semibold">
                    {user.username?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <Circle
                    className="absolute bottom-0 right-0 w-3 h-3 text-green-500 fill-green-500 bg-white rounded-full"
                  />
                </div>

                <div className="flex-1 min-w-0">
                  <div className="font-medium text-gray-900 truncate">
                    {user.username}
                    {user.email === currentUserEmail && (
                      <span className="ml-2 text-xs text-blue-600 font-normal">
                        (You)
                      </span>
                    )}
                  </div>
                  <div className="text-sm text-gray-500 truncate">
                    {user.email}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
