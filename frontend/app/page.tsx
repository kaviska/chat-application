'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/context';
import { MessageCircle } from 'lucide-react';
import Link from 'next/link';

export default function Home() {
  const { isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/chat');
    }
  }, [isAuthenticated, router]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 flex items-center justify-center p-4">
      <div className="max-w-4xl w-full text-center">
        <div className="mb-8 flex justify-center">
          <div className="bg-blue-600 p-6 rounded-full">
            <MessageCircle className="w-16 h-16 text-white" />
          </div>
        </div>
        
        <h1 className="text-5xl font-bold text-gray-900 mb-4">
          Real-Time Chat Application
        </h1>
        
        <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          Connect with people instantly. Secure, fast, and reliable messaging powered by Java and Next.js.
        </p>

        <div className="flex gap-4 justify-center mb-12">
          <Link
            href="/login"
            className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold text-lg"
          >
            Login
          </Link>
          <Link
            href="/register"
            className="px-8 py-3 bg-white text-blue-600 border-2 border-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-semibold text-lg"
          >
            Sign Up
          </Link>
        </div>

        <div className="grid md:grid-cols-3 gap-6 mt-16">
          <div className="bg-white p-6 rounded-xl shadow-lg">
            <div className="text-3xl mb-3">🔒</div>
            <h3 className="font-semibold text-lg mb-2">Secure Authentication</h3>
            <p className="text-gray-600">
              Password hashing with BCrypt ensures your data is safe
            </p>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-lg">
            <div className="text-3xl mb-3">⚡</div>
            <h3 className="font-semibold text-lg mb-2">Real-Time Messaging</h3>
            <p className="text-gray-600">
              Instant message delivery using WebSocket technology
            </p>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-lg">
            <div className="text-3xl mb-3">💾</div>
            <h3 className="font-semibold text-lg mb-2">Message History</h3>
            <p className="text-gray-600">
              All messages stored in MySQL database
            </p>
          </div>
        </div>

        <div className="mt-16 text-sm text-gray-500">
          <p>Built with Java 17, Next.js 14, MySQL & WebSockets</p>
        </div>
      </div>
    </div>
  );
}
